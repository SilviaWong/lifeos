package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeos.business.entity.*;
import com.lifeos.business.mapper.*;
import com.lifeos.business.service.ReminderSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * 提醒规则扫描调度与触发实现（强幂等保证）
 */
@Service
public class ReminderSchedulerServiceImpl implements ReminderSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ReminderSchedulerServiceImpl.class);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ReminderRuleMapper reminderRuleMapper;
    private final ReminderMessageMapper reminderMessageMapper;
    private final ReminderExecutionLogMapper reminderExecutionLogMapper;
    private final TodoTaskMapper todoTaskMapper;
    private final HabitMapper habitMapper;
    private final HabitCheckinMapper habitCheckinMapper;
    private final GoalMapper goalMapper;

    public ReminderSchedulerServiceImpl(ReminderRuleMapper reminderRuleMapper,
                                        ReminderMessageMapper reminderMessageMapper,
                                        ReminderExecutionLogMapper reminderExecutionLogMapper,
                                        TodoTaskMapper todoTaskMapper,
                                        HabitMapper habitMapper,
                                        HabitCheckinMapper habitCheckinMapper,
                                        GoalMapper goalMapper) {
        this.reminderRuleMapper = reminderRuleMapper;
        this.reminderMessageMapper = reminderMessageMapper;
        this.reminderExecutionLogMapper = reminderExecutionLogMapper;
        this.todoTaskMapper = todoTaskMapper;
        this.habitMapper = habitMapper;
        this.habitCheckinMapper = habitCheckinMapper;
        this.goalMapper = goalMapper;
    }

    @Override
    public int scanAndTriggerReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        // 1. 查询所有启用中且未软删除的提醒规则
        LambdaQueryWrapper<ReminderRule> qw = new LambdaQueryWrapper<>();
        qw.eq(ReminderRule::getStatus, 1)
          .eq(ReminderRule::getDeleted, 0);

        List<ReminderRule> activeRules = reminderRuleMapper.selectList(qw);
        if (activeRules == null || activeRules.isEmpty()) {
            return 0;
        }

        int triggeredCount = 0;

        for (ReminderRule rule : activeRules) {
            try {
                // 校验生效日期范围
                if (rule.getStartDate() != null && today.isBefore(rule.getStartDate())) {
                    continue;
                }
                if (rule.getEndDate() != null && today.isAfter(rule.getEndDate())) {
                    // 已过期，置为停用
                    disableRule(rule);
                    continue;
                }

                // 校验周期重复规则
                if (!matchesRepeatRule(rule.getRepeatRule(), today.getDayOfWeek())) {
                    continue;
                }

                // 根据触发类型处理
                String triggerType = rule.getTriggerType() != null ? rule.getTriggerType().toUpperCase() : "";
                int count = switch (triggerType) {
                    case "TODO_DUE" -> processTodoDueReminder(rule, now);
                    case "HABIT_CHECKIN" -> processHabitCheckinReminder(rule, today, currentTime);
                    case "GOAL_DUE" -> processGoalDueReminder(rule, today);
                    case "CUSTOM" -> processCustomReminder(rule, today, currentTime);
                    default -> 0;
                };

                triggeredCount += count;

                // 如果是单次规则且已触发，自动停用
                if (count > 0 && "ONCE".equalsIgnoreCase(rule.getRepeatRule())) {
                    disableRule(rule);
                }
            } catch (Exception e) {
                log.error("处理提醒规则 [{}] 异常: {}", rule.getId(), e.getMessage(), e);
            }
        }

        if (triggeredCount > 0) {
            log.info("提醒扫描完成，共生成 {} 条站内提醒消息", triggeredCount);
        }

        return triggeredCount;
    }

    /**
     * 处理待办任务截止提醒
     */
    private int processTodoDueReminder(ReminderRule rule, LocalDateTime now) {
        List<TodoTask> tasksToScan;
        if (rule.getBusinessRefId() != null) {
            TodoTask single = todoTaskMapper.selectById(rule.getBusinessRefId());
            tasksToScan = (single != null && single.getDeleted() == 0) ? List.of(single) : Collections.emptyList();
        } else {
            LambdaQueryWrapper<TodoTask> tqw = new LambdaQueryWrapper<>();
            tqw.eq(TodoTask::getUserId, rule.getUserId())
               .in(TodoTask::getStatus, "TODO", "IN_PROGRESS")
               .eq(TodoTask::getDeleted, 0)
               .isNotNull(TodoTask::getDueAt);
            tasksToScan = todoTaskMapper.selectList(tqw);
        }

        int count = 0;
        int advanceMinutes = rule.getAdvanceMinutes() != null ? rule.getAdvanceMinutes() : 0;

        for (TodoTask task : tasksToScan) {
            boolean isPending = "TODO".equalsIgnoreCase(task.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(task.getStatus());
            if (!isPending || task.getDueAt() == null) {
                continue;
            }

            // 提前提醒时间点
            LocalDateTime triggerTime = task.getDueAt().minusMinutes(advanceMinutes);

            // 当且仅当当前时间已经到达提前提醒时间点，且待办任务未超时过久（24小时内）
            if (now.isAfter(triggerTime) && task.getDueAt().isAfter(now.minusHours(24))) {
                String idempotencyKey = String.format("TODO_DUE:rule_%d:todo_%d:due_%s",
                        rule.getId(), task.getId(), task.getDueAt().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));

                boolean acquired = tryAcquireIdempotency(rule.getId(), rule.getUserId(), idempotencyKey);
                if (!acquired) {
                    continue;
                }

                String title = "待办截止提醒：" + task.getTitle();
                String content = String.format("您的待办任务「%s」将于 %s 截止，请合理安排时间并及时完成。",
                        task.getTitle(), task.getDueAt().format(TIME_FMT));

                createReminderMessage(rule, title, content, "TODO", task.getId());
                count++;
            }
        }

        return count;
    }

    /**
     * 处理习惯打卡提醒
     */
    private int processHabitCheckinReminder(ReminderRule rule, LocalDate today, LocalTime currentTime) {
        // 时间到达检查
        if (rule.getRemindAt() != null && currentTime.isBefore(rule.getRemindAt())) {
            return 0;
        }

        List<Habit> habitsToScan;
        if (rule.getBusinessRefId() != null) {
            Habit single = habitMapper.selectById(rule.getBusinessRefId());
            habitsToScan = (single != null && single.getDeleted() == 0) ? List.of(single) : Collections.emptyList();
        } else {
            LambdaQueryWrapper<Habit> hqw = new LambdaQueryWrapper<>();
            hqw.eq(Habit::getUserId, rule.getUserId())
               .eq(Habit::getStatus, "ACTIVE")
               .eq(Habit::getDeleted, 0);
            habitsToScan = habitMapper.selectList(hqw);
        }

        int count = 0;

        for (Habit habit : habitsToScan) {
            if (!"ACTIVE".equalsIgnoreCase(habit.getStatus())) {
                continue;
            }

            // 检查今日是否已经打卡
            LambdaQueryWrapper<HabitCheckin> cqw = new LambdaQueryWrapper<>();
            cqw.eq(HabitCheckin::getHabitId, habit.getId())
               .eq(HabitCheckin::getCheckinDate, today)
               .eq(HabitCheckin::getDeleted, 0);

            if (habitCheckinMapper.selectCount(cqw) > 0) {
                // 今日已打卡，无需提醒
                continue;
            }

            String idempotencyKey = String.format("HABIT_CHECKIN:rule_%d:habit_%d:date_%s",
                    rule.getId(), habit.getId(), today);

            boolean acquired = tryAcquireIdempotency(rule.getId(), rule.getUserId(), idempotencyKey);
            if (!acquired) {
                continue;
            }

            String title = "习惯打卡提醒：" + habit.getName();
            String content = String.format("您今天尚未完成习惯「%s」的打卡记录，自律创造自由，快去打卡吧！", habit.getName());

            createReminderMessage(rule, title, content, "HABIT", habit.getId());
            count++;
        }

        return count;
    }

    /**
     * 处理目标到期提醒
     */
    private int processGoalDueReminder(ReminderRule rule, LocalDate today) {
        List<Goal> goalsToScan;
        if (rule.getBusinessRefId() != null) {
            Goal single = goalMapper.selectById(rule.getBusinessRefId());
            goalsToScan = (single != null && single.getDeleted() == 0) ? List.of(single) : Collections.emptyList();
        } else {
            LambdaQueryWrapper<Goal> gqw = new LambdaQueryWrapper<>();
            gqw.eq(Goal::getUserId, rule.getUserId())
               .in(Goal::getStatus, "IN_PROGRESS", "NOT_STARTED")
               .eq(Goal::getDeleted, 0)
               .isNotNull(Goal::getDueDate);
            goalsToScan = goalMapper.selectList(gqw);
        }

        int count = 0;
        // 提前天数转换（默认 3 天，若设置了 advanceMinutes 则换算为天）
        int advanceDays = rule.getAdvanceMinutes() != null && rule.getAdvanceMinutes() > 0
                ? Math.max(1, rule.getAdvanceMinutes() / (24 * 60))
                : 3;

        for (Goal goal : goalsToScan) {
            if (goal.getDueDate() == null) {
                continue;
            }

            // 如果当前日期加上提前预警天数已到达或超过目标截止日期，且未过期超过 7 天
            if (!today.plusDays(advanceDays).isBefore(goal.getDueDate()) && !today.minusDays(7).isAfter(goal.getDueDate())) {
                String idempotencyKey = String.format("GOAL_DUE:rule_%d:goal_%d:date_%s",
                        rule.getId(), goal.getId(), today);

                boolean acquired = tryAcquireIdempotency(rule.getId(), rule.getUserId(), idempotencyKey);
                if (!acquired) {
                    continue;
                }

                String title = "目标即将到期提醒：" + goal.getTitle();
                int progressVal = goal.getProgress() != null ? goal.getProgress().intValue() : 0;
                String content = String.format("您的目标「%s」将于 %s 到期（当前进度：%d%%），请及时回顾并全力冲刺！",
                        goal.getTitle(), goal.getDueDate(), progressVal);

                createReminderMessage(rule, title, content, "GOAL", goal.getId());
                count++;
            }
        }

        return count;
    }

    /**
     * 处理自定义提醒
     */
    private int processCustomReminder(ReminderRule rule, LocalDate today, LocalTime currentTime) {
        if (rule.getRemindAt() != null && currentTime.isBefore(rule.getRemindAt())) {
            return 0;
        }

        String idempotencyKey = String.format("CUSTOM:rule_%d:date_%s", rule.getId(), today);

        boolean acquired = tryAcquireIdempotency(rule.getId(), rule.getUserId(), idempotencyKey);
        if (!acquired) {
            return 0;
        }

        String title = "定时提醒：" + rule.getRuleName();
        String content = String.format("您设置的提醒规则「%s」已按计划触发，请留意相应安排。", rule.getRuleName());

        createReminderMessage(rule, title, content, "SYSTEM", rule.getBusinessRefId());
        return 1;
    }

    /**
     * 强幂等占位：通过数据库唯一键 uk_reminder_idempotency 防重复执行
     */
    private boolean tryAcquireIdempotency(Long ruleId, Long userId, String idempotencyKey) {
        ReminderExecutionLog execLog = new ReminderExecutionLog();
        execLog.setRuleId(ruleId);
        execLog.setUserId(userId);
        execLog.setIdempotencyKey(idempotencyKey);
        execLog.setExecuteAt(LocalDateTime.now());
        execLog.setResultStatus("SUCCESS");
        execLog.setCreatedAt(LocalDateTime.now());

        try {
            reminderExecutionLogMapper.insert(execLog);
            return true;
        } catch (DuplicateKeyException e) {
            log.debug("提醒执行幂等键重复，跳过重复执行: {}", idempotencyKey);
            return false;
        } catch (Exception e) {
            // 兜底查询
            LambdaQueryWrapper<ReminderExecutionLog> check = new LambdaQueryWrapper<>();
            check.eq(ReminderExecutionLog::getIdempotencyKey, idempotencyKey);
            if (reminderExecutionLogMapper.selectCount(check) > 0) {
                return false;
            }
            log.warn("保存提醒幂等执行日志异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 创建并持久化站内提醒消息
     */
    private void createReminderMessage(ReminderRule rule, String title, String content, String messageType, Long businessRefId) {
        ReminderMessage msg = new ReminderMessage();
        msg.setUserId(rule.getUserId());
        msg.setRuleId(rule.getId());
        msg.setTitle(title);
        msg.setContent(content);
        msg.setMessageType(messageType);
        msg.setBusinessRefId(businessRefId);
        msg.setScheduledAt(LocalDateTime.now());
        msg.setSentAt(LocalDateTime.now());
        msg.setStatus("SENT");
        msg.setCreatedAt(LocalDateTime.now());
        msg.setDeleted(0);

        reminderMessageMapper.insert(msg);
    }

    private void disableRule(ReminderRule rule) {
        rule.setStatus(0);
        rule.setUpdatedAt(LocalDateTime.now());
        reminderRuleMapper.updateById(rule);
    }

    private boolean matchesRepeatRule(String repeatRule, DayOfWeek dayOfWeek) {
        if (repeatRule == null || "DAILY".equalsIgnoreCase(repeatRule) || "ONCE".equalsIgnoreCase(repeatRule)) {
            return true;
        }
        if ("WORKDAY".equalsIgnoreCase(repeatRule)) {
            return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
        }
        if ("WEEKEND".equalsIgnoreCase(repeatRule)) {
            return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        }
        return true;
    }
}
