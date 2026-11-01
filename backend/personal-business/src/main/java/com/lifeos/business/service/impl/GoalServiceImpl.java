package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.dto.CreateGoalRequest;
import com.lifeos.business.dto.CreateGoalTaskRequest;
import com.lifeos.business.dto.GoalQueryRequest;
import com.lifeos.business.dto.UpdateGoalRequest;
import com.lifeos.business.dto.UpdateGoalStatusRequest;
import com.lifeos.business.dto.UpdateGoalTaskRequest;
import com.lifeos.business.dto.UpdateGoalTaskStatusRequest;
import com.lifeos.business.entity.Goal;
import com.lifeos.business.entity.GoalTask;
import com.lifeos.business.entity.TodoTask;
import com.lifeos.business.enums.GoalPriority;
import com.lifeos.business.enums.GoalStatus;
import com.lifeos.business.enums.GoalTaskStatus;
import com.lifeos.business.mapper.GoalMapper;
import com.lifeos.business.mapper.GoalTaskMapper;
import com.lifeos.business.mapper.TodoTaskMapper;
import com.lifeos.business.service.GoalService;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.vo.GoalSummaryVO;
import com.lifeos.business.vo.GoalTaskVO;
import com.lifeos.business.vo.GoalVO;
import com.lifeos.common.api.CommonErrorCode;
import com.lifeos.common.api.PageResult;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.system.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 目标与子任务业务服务实现
 */
@Service
public class GoalServiceImpl implements GoalService {

    private static final Logger log = LoggerFactory.getLogger(GoalServiceImpl.class);

    private final GoalMapper goalMapper;
    private final GoalTaskMapper goalTaskMapper;
    private final TodoTaskMapper todoTaskMapper;
    private final ModuleUsageLogService moduleUsageLogService;

    public GoalServiceImpl(GoalMapper goalMapper,
                           GoalTaskMapper goalTaskMapper,
                           TodoTaskMapper todoTaskMapper,
                           ModuleUsageLogService moduleUsageLogService) {
        this.goalMapper = goalMapper;
        this.goalTaskMapper = goalTaskMapper;
        this.todoTaskMapper = todoTaskMapper;
        this.moduleUsageLogService = moduleUsageLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GoalVO createGoal(CreateGoalRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        validateDateRange(request.getStartDate(), request.getDueDate());

        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle(request.getTitle().trim());
        goal.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        goal.setStartDate(request.getStartDate());
        goal.setDueDate(request.getDueDate());
        goal.setPriority(request.getPriority() != null ? request.getPriority() : 2);
        goal.setStatus(GoalStatus.NOT_STARTED.getCode());
        goal.setProgress(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal.setDeleted(0);

        goalMapper.insert(goal);

        moduleUsageLogService.logUsage(userId, "GOAL", "CREATE", goal.getId());
        log.info("Goal created: id={}, userId={}, title='{}'", goal.getId(), userId, goal.getTitle());

        return convertToVO(goal, Collections.emptyList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GoalVO updateGoal(Long id, UpdateGoalRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        Goal goal = getGoalAndCheckOwnership(id, userId);

        validateDateRange(request.getStartDate(), request.getDueDate());

        goal.setTitle(request.getTitle().trim());
        goal.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        goal.setStartDate(request.getStartDate());
        goal.setDueDate(request.getDueDate());
        if (request.getPriority() != null) {
            goal.setPriority(request.getPriority());
        }
        goal.setUpdatedAt(LocalDateTime.now());

        goalMapper.updateById(goal);

        moduleUsageLogService.logUsage(userId, "GOAL", "UPDATE", goal.getId());
        log.info("Goal updated: id={}, userId={}, title='{}'", goal.getId(), userId, goal.getTitle());

        List<GoalTask> tasks = fetchActiveTasks(goal.getId(), userId);
        return convertToVO(goal, tasks);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GoalVO updateGoalStatus(Long id, UpdateGoalStatusRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        Goal goal = getGoalAndCheckOwnership(id, userId);

        if (!GoalStatus.isValid(request.getStatus())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "不支持的目标状态: " + request.getStatus());
        }

        GoalStatus newStatus = GoalStatus.fromCode(request.getStatus());
        String oldStatus = goal.getStatus();

        if (Objects.equals(oldStatus, newStatus.getCode())) {
            List<GoalTask> tasks = fetchActiveTasks(goal.getId(), userId);
            return convertToVO(goal, tasks);
        }

        goal.setStatus(newStatus.getCode());
        goal.setUpdatedAt(LocalDateTime.now());

        if (newStatus == GoalStatus.COMPLETED) {
            goal.setCompletedAt(LocalDateTime.now());
        } else {
            goal.setCompletedAt(null);
        }

        goalMapper.updateById(goal);

        moduleUsageLogService.logUsage(userId, "GOAL", "STATUS_" + newStatus.getCode(), goal.getId());
        log.info("Goal status changed: id={}, userId={}, oldStatus={}, newStatus={}", id, userId, oldStatus, newStatus.getCode());

        List<GoalTask> tasks = fetchActiveTasks(goal.getId(), userId);
        return convertToVO(goal, tasks);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoal(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        Goal goal = getGoalAndCheckOwnership(id, userId);

        // 1. 软删除目标本身
        goalMapper.deleteById(goal.getId());

        // 2. 软删除目标下所有子任务
        goalTaskMapper.delete(new LambdaQueryWrapper<GoalTask>()
                .eq(GoalTask::getGoalId, goal.getId())
                .eq(GoalTask::getUserId, userId));

        // 3. 重要：解除关联待办的 goal_id，绝不级联误删待办
        todoTaskMapper.update(null, new LambdaUpdateWrapper<TodoTask>()
                .eq(TodoTask::getGoalId, goal.getId())
                .eq(TodoTask::getUserId, userId)
                .set(TodoTask::getGoalId, null));

        moduleUsageLogService.logUsage(userId, "GOAL", "DELETE", id);
        log.info("Goal deleted and associated todos unlinked: id={}, userId={}", id, userId);
    }

    @Override
    public GoalVO getGoalById(Long id, boolean includeTasks) {
        Long userId = SecurityUtil.getLoginUserId();
        Goal goal = getGoalAndCheckOwnership(id, userId);

        List<GoalTask> tasks = fetchActiveTasks(goal.getId(), userId);
        GoalVO vo = convertToVO(goal, tasks);
        if (!includeTasks) {
            vo.setTasks(Collections.emptyList());
        }
        return vo;
    }

    @Override
    public PageResult<GoalVO> queryGoals(GoalQueryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        Page<Goal> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Goal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goal::getUserId, userId);

        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(Goal::getStatus, request.getStatus().trim());
        }

        if (request.getPriority() != null) {
            wrapper.eq(Goal::getPriority, request.getPriority());
        }

        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword().trim();
            wrapper.and(w -> w.like(Goal::getTitle, kw).or().like(Goal::getDescription, kw));
        }

        // 快捷筛选
        if (StringUtils.hasText(request.getFilterType()) && !"ALL".equalsIgnoreCase(request.getFilterType().trim())) {
            switch (request.getFilterType().trim().toUpperCase()) {
                case "ACTIVE" -> wrapper.in(Goal::getStatus, List.of(GoalStatus.NOT_STARTED.getCode(), GoalStatus.IN_PROGRESS.getCode()));
                case "COMPLETED" -> wrapper.eq(Goal::getStatus, GoalStatus.COMPLETED.getCode());
                case "ARCHIVED" -> wrapper.eq(Goal::getStatus, GoalStatus.ARCHIVED.getCode());
                default -> log.debug("Unknown filterType: {}, ignored", request.getFilterType());
            }
        }

        // 排序规则：进行中与未开始在前，优先级数值小的优先 (1高 > 2中 > 3低)，截止时间升序，ID 降序
        wrapper.last("ORDER BY CASE status WHEN 'IN_PROGRESS' THEN 1 WHEN 'NOT_STARTED' THEN 2 WHEN 'COMPLETED' THEN 3 WHEN 'ARCHIVED' THEN 4 ELSE 5 END ASC, priority ASC, due_date ASC, id DESC");

        Page<Goal> resultPage = goalMapper.selectPage(page, wrapper);

        List<GoalVO> voList = resultPage.getRecords().stream()
                .map(g -> {
                    List<GoalTask> tasks = fetchActiveTasks(g.getId(), userId);
                    GoalVO vo = convertToVO(g, tasks);
                    vo.setTasks(Collections.emptyList()); // 列表查询不携带全量子任务列表以减少传输负载
                    return vo;
                })
                .collect(Collectors.toList());

        return PageResult.of(voList, resultPage.getTotal(), (int) resultPage.getCurrent(), (int) resultPage.getSize());
    }

    @Override
    public GoalSummaryVO getGoalSummary() {
        Long userId = SecurityUtil.getLoginUserId();

        Long totalCount = goalMapper.selectCount(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getUserId, userId));

        Long activeCount = goalMapper.selectCount(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getUserId, userId)
                .in(Goal::getStatus, List.of(GoalStatus.NOT_STARTED.getCode(), GoalStatus.IN_PROGRESS.getCode())));

        Long completedCount = goalMapper.selectCount(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getUserId, userId)
                .eq(Goal::getStatus, GoalStatus.COMPLETED.getCode()));

        Long archivedCount = goalMapper.selectCount(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getUserId, userId)
                .eq(Goal::getStatus, GoalStatus.ARCHIVED.getCode()));

        // 计算活跃目标的平均完成进度
        List<Goal> activeGoals = goalMapper.selectList(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getUserId, userId)
                .in(Goal::getStatus, List.of(GoalStatus.NOT_STARTED.getCode(), GoalStatus.IN_PROGRESS.getCode())));

        BigDecimal avgProgress = BigDecimal.ZERO;
        if (!activeGoals.isEmpty()) {
            BigDecimal sum = activeGoals.stream()
                    .map(Goal::getProgress)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgProgress = sum.divide(BigDecimal.valueOf(activeGoals.size()), 2, RoundingMode.HALF_UP);
        }

        return new GoalSummaryVO(
                totalCount != null ? totalCount : 0L,
                activeCount != null ? activeCount : 0L,
                completedCount != null ? completedCount : 0L,
                archivedCount != null ? archivedCount : 0L,
                avgProgress
        );
    }

    // ==================== 子任务实现 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GoalTaskVO createGoalTask(Long goalId, CreateGoalTaskRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        Goal goal = getGoalAndCheckOwnership(goalId, userId);

        GoalTask task = new GoalTask();
        task.setUserId(userId);
        task.setGoalId(goal.getId());
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        task.setSortNo(request.getSortNo() != null ? request.getSortNo() : 0);
        task.setStatus(GoalTaskStatus.TODO.getCode());
        task.setDueAt(request.getDueAt());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setDeleted(0);

        goalTaskMapper.insert(task);

        // 如果目标原本为未开始，添加任务后自动推进为进行中
        if (GoalStatus.NOT_STARTED.getCode().equals(goal.getStatus())) {
            goal.setStatus(GoalStatus.IN_PROGRESS.getCode());
        }

        // 重新计算并更新目标进度
        recalculateGoalProgress(goal, userId);

        moduleUsageLogService.logUsage(userId, "GOAL_TASK", "CREATE", task.getId());
        log.info("Goal task created: taskId={}, goalId={}, userId={}", task.getId(), goalId, userId);

        return convertToTaskVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GoalTaskVO updateGoalTask(Long goalId, Long taskId, UpdateGoalTaskRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        getGoalAndCheckOwnership(goalId, userId);
        GoalTask task = getGoalTaskAndCheckOwnership(taskId, goalId, userId);

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getSortNo() != null) {
            task.setSortNo(request.getSortNo());
        }
        task.setDueAt(request.getDueAt());
        task.setUpdatedAt(LocalDateTime.now());

        goalTaskMapper.updateById(task);

        moduleUsageLogService.logUsage(userId, "GOAL_TASK", "UPDATE", task.getId());
        log.info("Goal task updated: taskId={}, goalId={}, userId={}", task.getId(), goalId, userId);

        return convertToTaskVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GoalTaskVO updateGoalTaskStatus(Long goalId, Long taskId, UpdateGoalTaskStatusRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        Goal goal = getGoalAndCheckOwnership(goalId, userId);
        GoalTask task = getGoalTaskAndCheckOwnership(taskId, goalId, userId);

        if (!GoalTaskStatus.isValid(request.getStatus())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "不支持的子任务状态: " + request.getStatus());
        }

        GoalTaskStatus newStatus = GoalTaskStatus.fromCode(request.getStatus());
        String oldStatus = task.getStatus();

        if (Objects.equals(oldStatus, newStatus.getCode())) {
            return convertToTaskVO(task);
        }

        task.setStatus(newStatus.getCode());
        task.setUpdatedAt(LocalDateTime.now());

        if (newStatus == GoalTaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }

        goalTaskMapper.updateById(task);

        // 子任务状态变更后重新计算父级目标进度
        recalculateGoalProgress(goal, userId);

        moduleUsageLogService.logUsage(userId, "GOAL_TASK",
                newStatus == GoalTaskStatus.DONE ? "COMPLETE" : "REOPEN", task.getId());
        log.info("Goal task status changed: taskId={}, goalId={}, oldStatus={}, newStatus={}",
                taskId, goalId, oldStatus, newStatus.getCode());

        return convertToTaskVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoalTask(Long goalId, Long taskId) {
        Long userId = SecurityUtil.getLoginUserId();
        Goal goal = getGoalAndCheckOwnership(goalId, userId);
        GoalTask task = getGoalTaskAndCheckOwnership(taskId, goalId, userId);

        goalTaskMapper.deleteById(task.getId());

        // 子任务删除后重新计算父级目标进度
        recalculateGoalProgress(goal, userId);

        moduleUsageLogService.logUsage(userId, "GOAL_TASK", "DELETE", taskId);
        log.info("Goal task deleted: taskId={}, goalId={}, userId={}", taskId, goalId, userId);
    }

    @Override
    public List<GoalTaskVO> getGoalTasks(Long goalId) {
        Long userId = SecurityUtil.getLoginUserId();
        getGoalAndCheckOwnership(goalId, userId);

        List<GoalTask> tasks = fetchActiveTasks(goalId, userId);
        return tasks.stream().map(this::convertToTaskVO).collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================

    private void recalculateGoalProgress(Goal goal, Long userId) {
        List<GoalTask> tasks = fetchActiveTasks(goal.getId(), userId);
        int total = tasks.size();

        if (total == 0) {
            // 没有子任务时进度为 0，展示“尚未拆解”，不生成虚假百分比
            goal.setProgress(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        } else {
            long completed = tasks.stream()
                    .filter(t -> GoalTaskStatus.DONE.getCode().equals(t.getStatus()))
                    .count();
            BigDecimal progress = BigDecimal.valueOf(completed)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            goal.setProgress(progress);
        }

        goal.setUpdatedAt(LocalDateTime.now());
        goalMapper.updateById(goal);
    }

    private List<GoalTask> fetchActiveTasks(Long goalId, Long userId) {
        return goalTaskMapper.selectList(new LambdaQueryWrapper<GoalTask>()
                .eq(GoalTask::getGoalId, goalId)
                .eq(GoalTask::getUserId, userId)
                .orderByAsc(GoalTask::getSortNo)
                .orderByAsc(GoalTask::getId));
    }

    private Goal getGoalAndCheckOwnership(Long goalId, Long userId) {
        if (goalId == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "目标 ID 不能为空");
        }
        Goal goal = goalMapper.selectOne(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getId, goalId)
                .eq(Goal::getUserId, userId));
        if (goal == null) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "目标不存在或无权访问");
        }
        return goal;
    }

    private GoalTask getGoalTaskAndCheckOwnership(Long taskId, Long goalId, Long userId) {
        if (taskId == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "子任务 ID 不能为空");
        }
        GoalTask task = goalTaskMapper.selectOne(new LambdaQueryWrapper<GoalTask>()
                .eq(GoalTask::getId, taskId)
                .eq(GoalTask::getGoalId, goalId)
                .eq(GoalTask::getUserId, userId));
        if (task == null) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "子任务不存在或无权访问");
        }
        return task;
    }

    private void validateDateRange(LocalDate startDate, LocalDate dueDate) {
        if (startDate != null && dueDate != null && startDate.isAfter(dueDate)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "开始日期不得晚于截止日期");
        }
    }

    private GoalVO convertToVO(Goal goal, List<GoalTask> tasks) {
        GoalVO vo = new GoalVO();
        vo.setId(goal.getId());
        vo.setUserId(goal.getUserId());
        vo.setTitle(goal.getTitle());
        vo.setDescription(goal.getDescription());
        vo.setStartDate(goal.getStartDate());
        vo.setDueDate(goal.getDueDate());
        vo.setPriority(goal.getPriority());
        vo.setPriorityDesc(GoalPriority.getDescriptionByValue(goal.getPriority()));
        vo.setStatus(goal.getStatus());

        GoalStatus statusEnum = GoalStatus.fromCode(goal.getStatus());
        vo.setStatusDesc(statusEnum != null ? statusEnum.getDescription() : goal.getStatus());

        vo.setProgress(goal.getProgress() != null ? goal.getProgress() : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        int totalCount = tasks.size();
        int completedCount = (int) tasks.stream()
                .filter(t -> GoalTaskStatus.DONE.getCode().equals(t.getStatus()))
                .count();

        vo.setTotalTaskCount(totalCount);
        vo.setCompletedTaskCount(completedCount);

        // 格式化文本表达：无子任务时展示“尚未拆解”
        if (totalCount == 0) {
            vo.setProgressText("尚未拆解");
        } else {
            vo.setProgressText(vo.getProgress().stripTrailingZeros().toPlainString() + "%");
        }

        vo.setCompletedAt(goal.getCompletedAt());
        vo.setCreatedAt(goal.getCreatedAt());
        vo.setUpdatedAt(goal.getUpdatedAt());

        vo.setTasks(tasks.stream().map(this::convertToTaskVO).collect(Collectors.toList()));

        return vo;
    }

    private GoalTaskVO convertToTaskVO(GoalTask task) {
        GoalTaskVO vo = new GoalTaskVO();
        vo.setId(task.getId());
        vo.setUserId(task.getUserId());
        vo.setGoalId(task.getGoalId());
        vo.setTitle(task.getTitle());
        vo.setDescription(task.getDescription());
        vo.setSortNo(task.getSortNo());
        vo.setStatus(task.getStatus());

        GoalTaskStatus statusEnum = GoalTaskStatus.fromCode(task.getStatus());
        vo.setStatusDesc(statusEnum != null ? statusEnum.getDescription() : task.getStatus());

        vo.setDueAt(task.getDueAt());
        vo.setCompletedAt(task.getCompletedAt());
        vo.setCreatedAt(task.getCreatedAt());
        vo.setUpdatedAt(task.getUpdatedAt());

        return vo;
    }
}

