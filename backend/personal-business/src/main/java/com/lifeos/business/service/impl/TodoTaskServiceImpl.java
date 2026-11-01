package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.dto.CreateTodoRequest;
import com.lifeos.business.dto.TodoQueryRequest;
import com.lifeos.business.dto.UpdateTodoRequest;
import com.lifeos.business.dto.UpdateTodoStatusRequest;
import com.lifeos.business.entity.TodoTask;
import com.lifeos.business.enums.TodoPriority;
import com.lifeos.business.enums.TodoStatus;
import com.lifeos.business.mapper.TodoTaskMapper;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.service.TodoTaskService;
import com.lifeos.business.vo.TodoSummaryVO;
import com.lifeos.business.vo.TodoVO;
import com.lifeos.common.api.CommonErrorCode;
import com.lifeos.common.api.PageResult;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.system.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 待办任务业务服务实现
 */
@Service
public class TodoTaskServiceImpl implements TodoTaskService {

    private static final Logger log = LoggerFactory.getLogger(TodoTaskServiceImpl.class);

    private final TodoTaskMapper todoTaskMapper;
    private final ModuleUsageLogService moduleUsageLogService;

    public TodoTaskServiceImpl(TodoTaskMapper todoTaskMapper, ModuleUsageLogService moduleUsageLogService) {
        this.todoTaskMapper = todoTaskMapper;
        this.moduleUsageLogService = moduleUsageLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoVO createTodo(CreateTodoRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        validateTimeRange(request.getStartAt(), request.getDueAt());

        TodoTask task = new TodoTask();
        task.setUserId(userId);
        task.setGoalId(request.getGoalId());
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        task.setPriority(request.getPriority() != null ? request.getPriority() : 2);
        task.setStatus(TodoStatus.TODO.getCode());
        task.setStartAt(request.getStartAt());
        task.setDueAt(request.getDueAt());
        task.setRemindAt(request.getRemindAt());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setDeleted(0);

        todoTaskMapper.insert(task);

        // 记录关键操作审计日志
        moduleUsageLogService.logUsage(userId, "TODO", "CREATE", task.getId());
        log.info("Todo created: id={}, userId={}, title='{}'", task.getId(), userId, task.getTitle());

        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoVO updateTodo(Long id, UpdateTodoRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        TodoTask task = getTaskAndCheckOwnership(id, userId);

        validateTimeRange(request.getStartAt(), request.getDueAt());

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        task.setGoalId(request.getGoalId());
        task.setStartAt(request.getStartAt());
        task.setDueAt(request.getDueAt());
        task.setRemindAt(request.getRemindAt());
        task.setUpdatedAt(LocalDateTime.now());

        todoTaskMapper.updateById(task);

        moduleUsageLogService.logUsage(userId, "TODO", "UPDATE", task.getId());
        log.info("Todo updated: id={}, userId={}, title='{}'", task.getId(), userId, task.getTitle());

        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoVO updateTodoStatus(Long id, UpdateTodoStatusRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        TodoTask task = getTaskAndCheckOwnership(id, userId);

        if (!TodoStatus.isValid(request.getStatus())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "不支持的任务状态: " + request.getStatus());
        }

        TodoStatus newStatus = TodoStatus.fromCode(request.getStatus());
        String oldStatus = task.getStatus();

        if (Objects.equals(oldStatus, newStatus.getCode())) {
            return convertToVO(task);
        }

        task.setStatus(newStatus.getCode());
        task.setUpdatedAt(LocalDateTime.now());

        if (newStatus == TodoStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (newStatus == TodoStatus.TODO || newStatus == TodoStatus.IN_PROGRESS) {
            // 重新打开或重新进行中时清空完成时间
            task.setCompletedAt(null);
        }

        todoTaskMapper.updateById(task);

        String action = (newStatus == TodoStatus.DONE) ? "COMPLETE" :
                ("DONE".equals(oldStatus) ? "REOPEN" : "STATUS_CHANGE");
        moduleUsageLogService.logUsage(userId, "TODO", action, task.getId());
        log.info("Todo status changed: id={}, userId={}, oldStatus={}, newStatus={}", id, userId, oldStatus, newStatus.getCode());

        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTodo(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        TodoTask task = getTaskAndCheckOwnership(id, userId);

        todoTaskMapper.deleteById(task.getId());
        moduleUsageLogService.logUsage(userId, "TODO", "DELETE", id);
        log.info("Todo deleted: id={}, userId={}", id, userId);
    }

    @Override
    public TodoVO getTodoById(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        TodoTask task = getTaskAndCheckOwnership(id, userId);
        return convertToVO(task);
    }

    @Override
    public PageResult<TodoVO> queryTodos(TodoQueryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        Page<TodoTask> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<TodoTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TodoTask::getUserId, userId);

        // 状态筛选
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(TodoTask::getStatus, request.getStatus().trim());
        }

        // 优先级筛选
        if (request.getPriority() != null) {
            wrapper.eq(TodoTask::getPriority, request.getPriority());
        }

        // 关联目标筛选
        if (request.getGoalId() != null) {
            wrapper.eq(TodoTask::getGoalId, request.getGoalId());
        }

        // 关键词模糊搜索
        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword().trim();
            wrapper.and(w -> w.like(TodoTask::getTitle, kw).or().like(TodoTask::getDescription, kw));
        }

        // 快捷筛选
        applyFilterType(wrapper, request.getFilterType());

        // 日期范围筛选（针对截止日期）
        if (request.getStartDate() != null) {
            wrapper.ge(TodoTask::getDueAt, request.getStartDate().atStartOfDay());
        }
        if (request.getEndDate() != null) {
            wrapper.le(TodoTask::getDueAt, request.getEndDate().atTime(LocalTime.MAX));
        }

        // 排序规则：未完成在前（TODO, IN_PROGRESS 先于 DONE, CANCELLED），优先级降序，截止时间升序，ID 降序
        wrapper.last("ORDER BY CASE status WHEN 'TODO' THEN 1 WHEN 'IN_PROGRESS' THEN 2 WHEN 'DONE' THEN 3 WHEN 'CANCELLED' THEN 4 ELSE 5 END ASC, priority DESC, due_at ASC, id DESC");

        Page<TodoTask> resultPage = todoTaskMapper.selectPage(page, wrapper);

        List<TodoVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, resultPage.getTotal(), (int) resultPage.getCurrent(), (int) resultPage.getSize());
    }

    @Override
    public TodoSummaryVO getTodoSummary() {
        Long userId = SecurityUtil.getLoginUserId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // 全部非删除待办总数
        Long totalCount = todoTaskMapper.selectCount(new LambdaQueryWrapper<TodoTask>()
                .eq(TodoTask::getUserId, userId));

        // 今日待办总数（截止在今日，或今日已完成，或计划在今日）
        Long todayTotalCount = todoTaskMapper.selectCount(new LambdaQueryWrapper<TodoTask>()
                .eq(TodoTask::getUserId, userId)
                .and(w -> w.between(TodoTask::getDueAt, todayStart, todayEnd)
                        .or().between(TodoTask::getCompletedAt, todayStart, todayEnd)
                        .or().between(TodoTask::getStartAt, todayStart, todayEnd)));

        // 今日已完成数
        Long todayCompletedCount = todoTaskMapper.selectCount(new LambdaQueryWrapper<TodoTask>()
                .eq(TodoTask::getUserId, userId)
                .eq(TodoTask::getStatus, TodoStatus.DONE.getCode())
                .between(TodoTask::getCompletedAt, todayStart, todayEnd));

        // 待处理任务总数（TODO + IN_PROGRESS）
        Long pendingCount = todoTaskMapper.selectCount(new LambdaQueryWrapper<TodoTask>()
                .eq(TodoTask::getUserId, userId)
                .in(TodoTask::getStatus, List.of(TodoStatus.TODO.getCode(), TodoStatus.IN_PROGRESS.getCode())));

        // 累计已完成任务总数
        Long completedCount = todoTaskMapper.selectCount(new LambdaQueryWrapper<TodoTask>()
                .eq(TodoTask::getUserId, userId)
                .eq(TodoTask::getStatus, TodoStatus.DONE.getCode()));

        // 已逾期待办数（未完成且截止时间早于当前系统时间）
        Long overdueCount = todoTaskMapper.selectCount(new LambdaQueryWrapper<TodoTask>()
                .eq(TodoTask::getUserId, userId)
                .in(TodoTask::getStatus, List.of(TodoStatus.TODO.getCode(), TodoStatus.IN_PROGRESS.getCode()))
                .lt(TodoTask::getDueAt, now));

        return new TodoSummaryVO(
                totalCount != null ? totalCount : 0L,
                todayTotalCount != null ? todayTotalCount : 0L,
                todayCompletedCount != null ? todayCompletedCount : 0L,
                pendingCount != null ? pendingCount : 0L,
                completedCount != null ? completedCount : 0L,
                overdueCount != null ? overdueCount : 0L
        );
    }

    private void applyFilterType(LambdaQueryWrapper<TodoTask> wrapper, String filterType) {
        if (!StringUtils.hasText(filterType) || "ALL".equalsIgnoreCase(filterType.trim())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        switch (filterType.trim().toUpperCase()) {
            case "TODAY" -> {
                LocalDateTime todayStart = today.atStartOfDay();
                LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
                wrapper.and(w -> w.between(TodoTask::getDueAt, todayStart, todayEnd)
                        .or().between(TodoTask::getCompletedAt, todayStart, todayEnd)
                        .or().between(TodoTask::getStartAt, todayStart, todayEnd));
            }
            case "WEEK" -> {
                LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                LocalDateTime weekStart = monday.atStartOfDay();
                LocalDateTime weekEnd = sunday.atTime(LocalTime.MAX);
                wrapper.and(w -> w.between(TodoTask::getDueAt, weekStart, weekEnd)
                        .or().between(TodoTask::getCompletedAt, weekStart, weekEnd)
                        .or().between(TodoTask::getStartAt, weekStart, weekEnd));
            }
            case "OVERDUE" -> {
                wrapper.in(TodoTask::getStatus, List.of(TodoStatus.TODO.getCode(), TodoStatus.IN_PROGRESS.getCode()))
                        .lt(TodoTask::getDueAt, now);
            }
            case "PENDING" -> {
                wrapper.in(TodoTask::getStatus, List.of(TodoStatus.TODO.getCode(), TodoStatus.IN_PROGRESS.getCode()));
            }
            case "COMPLETED" -> {
                wrapper.eq(TodoTask::getStatus, TodoStatus.DONE.getCode());
            }
            default -> log.debug("Unknown filterType: {}, ignored", filterType);
        }
    }

    private TodoTask getTaskAndCheckOwnership(Long id, Long userId) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "待办 ID 不能为空");
        }
        TodoTask task = todoTaskMapper.selectOne(new LambdaQueryWrapper<TodoTask>()
                .eq(TodoTask::getId, id)
                .eq(TodoTask::getUserId, userId));

        if (task == null) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "待办任务不存在或无权访问");
        }
        return task;
    }

    private void validateTimeRange(LocalDateTime startAt, LocalDateTime dueAt) {
        if (startAt != null && dueAt != null && startAt.isAfter(dueAt)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "计划开始时间不得晚于截止时间");
        }
    }

    private TodoVO convertToVO(TodoTask task) {
        TodoVO vo = new TodoVO();
        vo.setId(task.getId());
        vo.setUserId(task.getUserId());
        vo.setGoalId(task.getGoalId());
        vo.setTitle(task.getTitle());
        vo.setDescription(task.getDescription());
        vo.setPriority(task.getPriority());
        vo.setPriorityDesc(TodoPriority.getDescriptionByValue(task.getPriority()));
        vo.setStatus(task.getStatus());

        TodoStatus statusEnum = TodoStatus.fromCode(task.getStatus());
        vo.setStatusDesc(statusEnum != null ? statusEnum.getDescription() : task.getStatus());

        vo.setStartAt(task.getStartAt());
        vo.setDueAt(task.getDueAt());
        vo.setRemindAt(task.getRemindAt());
        vo.setCompletedAt(task.getCompletedAt());
        vo.setCreatedAt(task.getCreatedAt());
        vo.setUpdatedAt(task.getUpdatedAt());

        // 判断是否已逾期：未完成状态且截止时间早于当前时间
        boolean isOverdue = false;
        if (task.getDueAt() != null &&
                !TodoStatus.DONE.getCode().equals(task.getStatus()) &&
                !TodoStatus.CANCELLED.getCode().equals(task.getStatus())) {
            isOverdue = task.getDueAt().isBefore(LocalDateTime.now());
        }
        vo.setIsOverdue(isOverdue);

        return vo;
    }
}

