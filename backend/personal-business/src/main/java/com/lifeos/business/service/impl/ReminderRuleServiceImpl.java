package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.dto.CreateReminderRuleRequest;
import com.lifeos.business.dto.ReminderRuleQueryRequest;
import com.lifeos.business.dto.UpdateReminderRuleRequest;
import com.lifeos.business.entity.Goal;
import com.lifeos.business.entity.Habit;
import com.lifeos.business.entity.ReminderRule;
import com.lifeos.business.entity.TodoTask;
import com.lifeos.business.mapper.GoalMapper;
import com.lifeos.business.mapper.HabitMapper;
import com.lifeos.business.mapper.ReminderRuleMapper;
import com.lifeos.business.mapper.TodoTaskMapper;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.service.ReminderRuleService;
import com.lifeos.business.vo.ReminderRuleVO;
import com.lifeos.common.api.PageResult;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.system.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 提醒规则业务实现
 */
@Service
public class ReminderRuleServiceImpl implements ReminderRuleService {

    private final ReminderRuleMapper reminderRuleMapper;
    private final TodoTaskMapper todoTaskMapper;
    private final HabitMapper habitMapper;
    private final GoalMapper goalMapper;
    private final ModuleUsageLogService moduleUsageLogService;

    public ReminderRuleServiceImpl(ReminderRuleMapper reminderRuleMapper,
                                   TodoTaskMapper todoTaskMapper,
                                   HabitMapper habitMapper,
                                   GoalMapper goalMapper,
                                   ModuleUsageLogService moduleUsageLogService) {
        this.reminderRuleMapper = reminderRuleMapper;
        this.todoTaskMapper = todoTaskMapper;
        this.habitMapper = habitMapper;
        this.goalMapper = goalMapper;
        this.moduleUsageLogService = moduleUsageLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReminderRuleVO createRule(CreateReminderRuleRequest request) {
        Long userId = SecurityUtil.getLoginUserId();

        validateRuleDates(request.getStartDate(), request.getEndDate());
        validateBusinessRef(request.getTriggerType(), request.getBusinessRefId(), userId);

        ReminderRule rule = new ReminderRule();
        rule.setUserId(userId);
        rule.setRuleName(request.getRuleName().trim());
        rule.setTriggerType(request.getTriggerType().trim().toUpperCase());
        rule.setBusinessRefId(request.getBusinessRefId());
        rule.setRemindAt(request.getRemindAt());
        rule.setAdvanceMinutes(request.getAdvanceMinutes() != null ? Math.max(0, request.getAdvanceMinutes()) : 0);
        rule.setRepeatRule(StringUtils.hasText(request.getRepeatRule()) ? request.getRepeatRule().trim().toUpperCase() : "DAILY");
        rule.setStartDate(request.getStartDate());
        rule.setEndDate(request.getEndDate());
        rule.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        rule.setDeleted(0);

        reminderRuleMapper.insert(rule);
        moduleUsageLogService.logUsage(userId, "REMINDER_RULE", "CREATE", rule.getId());

        return convertToVO(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReminderRuleVO updateRule(Long id, UpdateReminderRuleRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        ReminderRule rule = getValidRule(id, userId);

        validateRuleDates(request.getStartDate(), request.getEndDate());
        validateBusinessRef(request.getTriggerType(), request.getBusinessRefId(), userId);

        rule.setRuleName(request.getRuleName().trim());
        rule.setTriggerType(request.getTriggerType().trim().toUpperCase());
        rule.setBusinessRefId(request.getBusinessRefId());
        rule.setRemindAt(request.getRemindAt());
        rule.setAdvanceMinutes(request.getAdvanceMinutes() != null ? Math.max(0, request.getAdvanceMinutes()) : 0);
        rule.setRepeatRule(StringUtils.hasText(request.getRepeatRule()) ? request.getRepeatRule().trim().toUpperCase() : "DAILY");
        rule.setStartDate(request.getStartDate());
        rule.setEndDate(request.getEndDate());
        if (request.getStatus() != null) {
            rule.setStatus(request.getStatus());
        }
        rule.setUpdatedAt(LocalDateTime.now());

        reminderRuleMapper.updateById(rule);
        moduleUsageLogService.logUsage(userId, "REMINDER_RULE", "UPDATE", rule.getId());

        return convertToVO(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        ReminderRule rule = getValidRule(id, userId);

        reminderRuleMapper.deleteById(rule.getId());
        moduleUsageLogService.logUsage(userId, "REMINDER_RULE", "DELETE", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReminderRuleVO toggleRuleStatus(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        ReminderRule rule = getValidRule(id, userId);

        int newStatus = rule.getStatus() != null && rule.getStatus() == 1 ? 0 : 1;
        rule.setStatus(newStatus);
        rule.setUpdatedAt(LocalDateTime.now());
        reminderRuleMapper.updateById(rule);

        moduleUsageLogService.logUsage(userId, "REMINDER_RULE", "TOGGLE_STATUS", id);
        return convertToVO(rule);
    }

    @Override
    public ReminderRuleVO getRule(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        ReminderRule rule = getValidRule(id, userId);
        return convertToVO(rule);
    }

    @Override
    public PageResult<ReminderRuleVO> queryRules(ReminderRuleQueryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        if (request == null) {
            request = new ReminderRuleQueryRequest();
        }

        Page<ReminderRule> pageParam = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<ReminderRule> qw = new LambdaQueryWrapper<>();
        qw.eq(ReminderRule::getUserId, userId);
        qw.eq(ReminderRule::getDeleted, 0);

        if (StringUtils.hasText(request.getTriggerType())) {
            qw.eq(ReminderRule::getTriggerType, request.getTriggerType().trim().toUpperCase());
        }

        if (request.getStatus() != null) {
            qw.eq(ReminderRule::getStatus, request.getStatus());
        }

        if (StringUtils.hasText(request.getKeyword())) {
            qw.like(ReminderRule::getRuleName, request.getKeyword().trim());
        }

        qw.orderByDesc(ReminderRule::getCreatedAt);

        Page<ReminderRule> pageResult = reminderRuleMapper.selectPage(pageParam, qw);
        List<ReminderRule> records = pageResult.getRecords();

        if (records == null || records.isEmpty()) {
            return PageResult.empty((int) pageResult.getCurrent(), (int) pageResult.getSize());
        }

        List<ReminderRuleVO> voList = new ArrayList<>(records.size());
        for (ReminderRule r : records) {
            voList.add(convertToVO(r));
        }

        return PageResult.of(voList, pageResult.getTotal(), (int) pageResult.getCurrent(), (int) pageResult.getSize());
    }

    private ReminderRule getValidRule(Long id, Long userId) {
        ReminderRule rule = reminderRuleMapper.selectById(id);
        if (rule == null || rule.getDeleted() != 0 || !rule.getUserId().equals(userId)) {
            throw new BusinessException(404, "提醒规则不存在或无权限访问");
        }
        return rule;
    }

    private void validateRuleDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(400, "生效开始日期不能晚于截止日期");
        }
    }

    private void validateBusinessRef(String triggerType, Long refId, Long userId) {
        if (refId == null) {
            return;
        }

        if ("TODO_DUE".equalsIgnoreCase(triggerType)) {
            TodoTask todo = todoTaskMapper.selectById(refId);
            if (todo == null || todo.getDeleted() != 0 || !todo.getUserId().equals(userId)) {
                throw new BusinessException(404, "关联的待办任务不存在或无权限");
            }
        } else if ("HABIT_CHECKIN".equalsIgnoreCase(triggerType)) {
            Habit habit = habitMapper.selectById(refId);
            if (habit == null || habit.getDeleted() != 0 || !habit.getUserId().equals(userId)) {
                throw new BusinessException(404, "关联的习惯不存在或无权限");
            }
        } else if ("GOAL_DUE".equalsIgnoreCase(triggerType)) {
            Goal goal = goalMapper.selectById(refId);
            if (goal == null || goal.getDeleted() != 0 || !goal.getUserId().equals(userId)) {
                throw new BusinessException(404, "关联的目标不存在或无权限");
            }
        }
    }

    private ReminderRuleVO convertToVO(ReminderRule r) {
        ReminderRuleVO vo = new ReminderRuleVO();
        vo.setId(r.getId());
        vo.setUserId(r.getUserId());
        vo.setRuleName(r.getRuleName());
        vo.setTriggerType(r.getTriggerType());
        vo.setTriggerTypeDesc(formatTriggerTypeDesc(r.getTriggerType()));
        vo.setBusinessRefId(r.getBusinessRefId());
        vo.setRemindAt(r.getRemindAt());
        vo.setAdvanceMinutes(r.getAdvanceMinutes());
        vo.setRepeatRule(r.getRepeatRule());
        vo.setRepeatRuleDesc(formatRepeatRuleDesc(r.getRepeatRule()));
        vo.setStartDate(r.getStartDate());
        vo.setEndDate(r.getEndDate());
        vo.setStatus(r.getStatus());
        vo.setStatusDesc(r.getStatus() != null && r.getStatus() == 1 ? "启用中" : "已停用");
        vo.setCreatedAt(r.getCreatedAt());
        vo.setUpdatedAt(r.getUpdatedAt());
        return vo;
    }

    private String formatTriggerTypeDesc(String type) {
        if (type == null) return "未知类型";
        return switch (type.toUpperCase()) {
            case "TODO_DUE" -> "待办截止提醒";
            case "HABIT_CHECKIN" -> "习惯打卡提醒";
            case "GOAL_DUE" -> "目标到期提醒";
            case "CUSTOM" -> "自定义定时提醒";
            default -> type;
        };
    }

    private String formatRepeatRuleDesc(String repeat) {
        if (repeat == null) return "每天";
        return switch (repeat.toUpperCase()) {
            case "DAILY" -> "每天";
            case "WORKDAY" -> "工作日";
            case "WEEKEND" -> "周末";
            case "ONCE" -> "单次";
            default -> repeat;
        };
    }
}
