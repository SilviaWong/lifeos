package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.dto.AbortPomodoroRequest;
import com.lifeos.business.dto.FinishPomodoroRequest;
import com.lifeos.business.dto.PomodoroQueryRequest;
import com.lifeos.business.dto.StartPomodoroRequest;
import com.lifeos.business.dto.UpdatePomodoroSettingRequest;
import com.lifeos.business.entity.Goal;
import com.lifeos.business.entity.PomodoroRecord;
import com.lifeos.business.entity.PomodoroSetting;
import com.lifeos.business.entity.TodoTask;
import com.lifeos.business.enums.PomodoroEndReason;
import com.lifeos.business.enums.PomodoroSessionType;
import com.lifeos.business.enums.PomodoroStatus;
import com.lifeos.business.mapper.GoalMapper;
import com.lifeos.business.mapper.PomodoroRecordMapper;
import com.lifeos.business.mapper.PomodoroSettingMapper;
import com.lifeos.business.mapper.TodoTaskMapper;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.service.PomodoroService;
import com.lifeos.business.vo.PomodoroRecordVO;
import com.lifeos.business.vo.PomodoroSettingVO;
import com.lifeos.business.vo.PomodoroSummaryVO;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 番茄钟专注会话与偏好设置业务服务实现
 */
@Service
public class PomodoroServiceImpl implements PomodoroService {

    private static final Logger log = LoggerFactory.getLogger(PomodoroServiceImpl.class);

    private final PomodoroRecordMapper pomodoroRecordMapper;
    private final PomodoroSettingMapper pomodoroSettingMapper;
    private final GoalMapper goalMapper;
    private final TodoTaskMapper todoTaskMapper;
    private final ModuleUsageLogService moduleUsageLogService;

    public PomodoroServiceImpl(PomodoroRecordMapper pomodoroRecordMapper,
                               PomodoroSettingMapper pomodoroSettingMapper,
                               GoalMapper goalMapper,
                               TodoTaskMapper todoTaskMapper,
                               ModuleUsageLogService moduleUsageLogService) {
        this.pomodoroRecordMapper = pomodoroRecordMapper;
        this.pomodoroSettingMapper = pomodoroSettingMapper;
        this.goalMapper = goalMapper;
        this.todoTaskMapper = todoTaskMapper;
        this.moduleUsageLogService = moduleUsageLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PomodoroRecordVO startSession(StartPomodoroRequest request) {
        if (request == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "请求参数不能为空");
        }
        Long userId = SecurityUtil.getLoginUserId();

        PomodoroSessionType sessionType = PomodoroSessionType.fromCode(request.getSessionType());
        if (sessionType == null) {
            sessionType = PomodoroSessionType.FOCUS;
        }

        if (request.getPlannedSeconds() == null || request.getPlannedSeconds() < 60 || request.getPlannedSeconds() > 14400) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "计划时长必须在 60 秒到 14400 秒 (4 小时) 之间");
        }

        // 校验关联的目标
        if (request.getGoalId() != null) {
            Goal goal = goalMapper.selectById(request.getGoalId());
            if (goal == null || !goal.getUserId().equals(userId)) {
                throw new BusinessException(CommonErrorCode.NOT_FOUND, "关联的目标不存在或无权访问");
            }
        }

        // 校验关联的待办任务
        if (request.getTodoId() != null) {
            TodoTask todo = todoTaskMapper.selectById(request.getTodoId());
            if (todo == null || !todo.getUserId().equals(userId)) {
                throw new BusinessException(CommonErrorCode.NOT_FOUND, "关联的待办任务不存在或无权访问");
            }
            // 若用户未显式传 goalId，可继承待办关联的 goalId
            if (request.getGoalId() == null && todo.getGoalId() != null) {
                request.setGoalId(todo.getGoalId());
            }
        }

        // 检查用户当前是否有活跃的会话 (RUNNING 或 PAUSED)
        List<PomodoroRecord> activeRecords = pomodoroRecordMapper.selectList(
                new LambdaQueryWrapper<PomodoroRecord>()
                        .eq(PomodoroRecord::getUserId, userId)
                        .in(PomodoroRecord::getStatus, List.of(PomodoroStatus.RUNNING.getCode(), PomodoroStatus.PAUSED.getCode()))
                        .orderByDesc(PomodoroRecord::getStartedAt)
        );

        if (!activeRecords.isEmpty()) {
            PomodoroRecord active = activeRecords.get(0);
            long elapsedHours = Duration.between(active.getStartedAt(), LocalDateTime.now()).toHours();
            if (elapsedHours >= 24) {
                // 超时 24 小时的僵尸会话自动超时收尾
                active.setStatus(PomodoroStatus.ABORTED.getCode());
                active.setEndedAt(active.getStartedAt().plusSeconds(active.getPlannedSeconds()));
                active.setEndReason(PomodoroEndReason.TIMEOUT.getCode());
                pomodoroRecordMapper.updateById(active);
                log.info("Automatically timed out stale session {} for user {}", active.getId(), userId);
            } else {
                throw new BusinessException(CommonErrorCode.BAD_REQUEST, "当前已有进行中的番茄钟会话，请先完成或中止后开启新会话");
            }
        }

        PomodoroRecord record = new PomodoroRecord();
        record.setUserId(userId);
        record.setGoalId(request.getGoalId());
        record.setTodoId(request.getTodoId());
        record.setSessionType(sessionType.getCode());
        record.setPlannedSeconds(request.getPlannedSeconds());
        record.setActualSeconds(0);
        record.setStatus(PomodoroStatus.RUNNING.getCode());
        record.setStartedAt(LocalDateTime.now());

        pomodoroRecordMapper.insert(record);
        moduleUsageLogService.logUsage(userId, "POMODORO", "START", record.getId());

        log.info("User {} started pomodoro session {} ({})", userId, record.getId(), sessionType.getCode());
        return convertSingle(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PomodoroRecordVO pauseSession(Long id) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "会话 ID 不能为空");
        }
        Long userId = SecurityUtil.getLoginUserId();
        PomodoroRecord record = findAndVerifyRecord(id, userId);

        if (PomodoroStatus.PAUSED.getCode().equals(record.getStatus())) {
            return convertSingle(record);
        }
        if (!PomodoroStatus.RUNNING.getCode().equals(record.getStatus())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "只有进行中的会话才可暂停");
        }

        record.setStatus(PomodoroStatus.PAUSED.getCode());
        pomodoroRecordMapper.updateById(record);
        moduleUsageLogService.logUsage(userId, "POMODORO", "PAUSE", record.getId());

        log.info("User {} paused pomodoro session {}", userId, record.getId());
        return convertSingle(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PomodoroRecordVO resumeSession(Long id) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "会话 ID 不能为空");
        }
        Long userId = SecurityUtil.getLoginUserId();
        PomodoroRecord record = findAndVerifyRecord(id, userId);

        if (PomodoroStatus.RUNNING.getCode().equals(record.getStatus())) {
            return convertSingle(record);
        }
        if (!PomodoroStatus.PAUSED.getCode().equals(record.getStatus())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "只有已暂停的会话才可继续");
        }

        record.setStatus(PomodoroStatus.RUNNING.getCode());
        pomodoroRecordMapper.updateById(record);
        moduleUsageLogService.logUsage(userId, "POMODORO", "RESUME", record.getId());

        log.info("User {} resumed pomodoro session {}", userId, record.getId());
        return convertSingle(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PomodoroRecordVO finishSession(Long id, FinishPomodoroRequest request) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "会话 ID 不能为空");
        }
        Long userId = SecurityUtil.getLoginUserId();
        PomodoroRecord record = findAndVerifyRecord(id, userId);

        // 幂等防重保障
        if (PomodoroStatus.COMPLETED.getCode().equals(record.getStatus())) {
            return convertSingle(record);
        }
        if (PomodoroStatus.ABORTED.getCode().equals(record.getStatus())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "已中止的会话无法重复完成");
        }

        LocalDateTime now = LocalDateTime.now();
        long serverElapsedSeconds = Duration.between(record.getStartedAt(), now).getSeconds();
        if (serverElapsedSeconds < 0) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "服务器时间校验失败，结束时间早于开始时间");
        }

        // 服务端时间差保护与校验机制：
        // 实际专注时长不能无限信任前端，最大允许时长不超过服务端真实经过时间加上网络缓冲区（15 秒）
        long maxAllowedSeconds = serverElapsedSeconds + 15;
        int actualSeconds;
        if (request != null && request.getClientActualSeconds() != null && request.getClientActualSeconds() > 0) {
            actualSeconds = (int) Math.min(request.getClientActualSeconds(),
                    Math.min(record.getPlannedSeconds(), maxAllowedSeconds));
        } else {
            actualSeconds = (int) Math.min(record.getPlannedSeconds(), Math.max(0, serverElapsedSeconds));
        }

        record.setStatus(PomodoroStatus.COMPLETED.getCode());
        record.setEndedAt(now);
        record.setActualSeconds(actualSeconds);
        record.setEndReason(PomodoroEndReason.NORMAL_FINISH.getCode());

        pomodoroRecordMapper.updateById(record);
        moduleUsageLogService.logUsage(userId, "POMODORO", "FINISH", record.getId());

        log.info("User {} finished pomodoro session {}, actualSeconds={}", userId, record.getId(), actualSeconds);
        return convertSingle(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PomodoroRecordVO abortSession(Long id, AbortPomodoroRequest request) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "会话 ID 不能为空");
        }
        Long userId = SecurityUtil.getLoginUserId();
        PomodoroRecord record = findAndVerifyRecord(id, userId);

        if (PomodoroStatus.ABORTED.getCode().equals(record.getStatus())) {
            return convertSingle(record);
        }
        if (PomodoroStatus.COMPLETED.getCode().equals(record.getStatus())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "已完成的会话无法重复中止");
        }

        LocalDateTime now = LocalDateTime.now();
        long serverElapsedSeconds = Math.max(0, Duration.between(record.getStartedAt(), now).getSeconds());
        int actualSeconds;
        if (request != null && request.getClientActualSeconds() != null && request.getClientActualSeconds() > 0) {
            actualSeconds = (int) Math.min(request.getClientActualSeconds(),
                    Math.min(record.getPlannedSeconds(), serverElapsedSeconds + 15));
        } else {
            actualSeconds = (int) Math.min(record.getPlannedSeconds(), serverElapsedSeconds);
        }

        String reason = (request != null && StringUtils.hasText(request.getReason()))
                ? request.getReason().trim()
                : PomodoroEndReason.USER_ABORT.getCode();

        record.setStatus(PomodoroStatus.ABORTED.getCode());
        record.setEndedAt(now);
        record.setActualSeconds(actualSeconds);
        record.setEndReason(reason);

        pomodoroRecordMapper.updateById(record);
        moduleUsageLogService.logUsage(userId, "POMODORO", "ABORT", record.getId());

        log.info("User {} aborted pomodoro session {}, reason={}", userId, record.getId(), reason);
        return convertSingle(record);
    }

    @Override
    public PomodoroRecordVO getCurrentSession() {
        Long userId = SecurityUtil.getLoginUserId();
        List<PomodoroRecord> records = pomodoroRecordMapper.selectList(
                new LambdaQueryWrapper<PomodoroRecord>()
                        .eq(PomodoroRecord::getUserId, userId)
                        .in(PomodoroRecord::getStatus, List.of(PomodoroStatus.RUNNING.getCode(), PomodoroStatus.PAUSED.getCode()))
                        .orderByDesc(PomodoroRecord::getStartedAt)
                        .last("LIMIT 1")
        );

        if (records.isEmpty()) {
            return null;
        }

        PomodoroRecord active = records.get(0);
        long elapsedHours = Duration.between(active.getStartedAt(), LocalDateTime.now()).toHours();
        if (elapsedHours >= 24) {
            // 自动清理过期僵尸会话
            active.setStatus(PomodoroStatus.ABORTED.getCode());
            active.setEndedAt(active.getStartedAt().plusSeconds(active.getPlannedSeconds()));
            active.setEndReason(PomodoroEndReason.TIMEOUT.getCode());
            pomodoroRecordMapper.updateById(active);
            return null;
        }

        return convertSingle(active);
    }

    @Override
    public PomodoroRecordVO getSessionById(Long id) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "会话 ID 不能为空");
        }
        Long userId = SecurityUtil.getLoginUserId();
        PomodoroRecord record = findAndVerifyRecord(id, userId);
        return convertSingle(record);
    }

    @Override
    public PageResult<PomodoroRecordVO> queryRecords(PomodoroQueryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();

        LambdaQueryWrapper<PomodoroRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PomodoroRecord::getUserId, userId);

        if (StringUtils.hasText(request.getSessionType())) {
            wrapper.eq(PomodoroRecord::getSessionType, request.getSessionType().trim().toUpperCase());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(PomodoroRecord::getStatus, request.getStatus().trim().toUpperCase());
        }
        if (request.getGoalId() != null) {
            wrapper.eq(PomodoroRecord::getGoalId, request.getGoalId());
        }
        if (request.getTodoId() != null) {
            wrapper.eq(PomodoroRecord::getTodoId, request.getTodoId());
        }
        if (request.getStartDate() != null) {
            wrapper.ge(PomodoroRecord::getStartedAt, request.getStartDate().atStartOfDay());
        }
        if (request.getEndDate() != null) {
            wrapper.le(PomodoroRecord::getStartedAt, request.getEndDate().atTime(23, 59, 59));
        }

        wrapper.orderByDesc(PomodoroRecord::getStartedAt)
                .orderByDesc(PomodoroRecord::getId);

        Page<PomodoroRecord> page = new Page<>(
                request.getPageNum() != null ? request.getPageNum() : 1L,
                request.getPageSize() != null ? request.getPageSize() : 10L
        );
        Page<PomodoroRecord> selectPage = pomodoroRecordMapper.selectPage(page, wrapper);

        Map<Long, String> goalTitleMap = fetchGoalTitlesMap(selectPage.getRecords());
        Map<Long, String> todoTitleMap = fetchTodoTitlesMap(selectPage.getRecords());
        List<PomodoroRecordVO> voList = selectPage.getRecords().stream()
                .map(r -> convertToVO(r, goalTitleMap, todoTitleMap))
                .collect(Collectors.toList());

        return PageResult.of(voList, selectPage.getTotal(), (int) selectPage.getCurrent(), (int) selectPage.getSize());
    }

    @Override
    public PomodoroSummaryVO getSummary() {
        Long userId = SecurityUtil.getLoginUserId();
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 1. 今日完成会话
        List<PomodoroRecord> todayCompletedRecords = pomodoroRecordMapper.selectList(
                new LambdaQueryWrapper<PomodoroRecord>()
                        .eq(PomodoroRecord::getUserId, userId)
                        .eq(PomodoroRecord::getSessionType, PomodoroSessionType.FOCUS.getCode())
                        .eq(PomodoroRecord::getStatus, PomodoroStatus.COMPLETED.getCode())
                        .ge(PomodoroRecord::getStartedAt, today.atStartOfDay())
                        .le(PomodoroRecord::getStartedAt, today.atTime(23, 59, 59))
        );

        long todayFocusSeconds = todayCompletedRecords.stream()
                .mapToLong(r -> r.getActualSeconds() != null ? r.getActualSeconds() : 0)
                .sum();
        int todayCompletedCount = todayCompletedRecords.size();

        // 2. 本周完成会话 (周一到今天)
        List<PomodoroRecord> weekCompletedRecords = pomodoroRecordMapper.selectList(
                new LambdaQueryWrapper<PomodoroRecord>()
                        .eq(PomodoroRecord::getUserId, userId)
                        .eq(PomodoroRecord::getSessionType, PomodoroSessionType.FOCUS.getCode())
                        .eq(PomodoroRecord::getStatus, PomodoroStatus.COMPLETED.getCode())
                        .ge(PomodoroRecord::getStartedAt, monday.atStartOfDay())
                        .le(PomodoroRecord::getStartedAt, today.atTime(23, 59, 59))
        );

        long weekFocusSeconds = weekCompletedRecords.stream()
                .mapToLong(r -> r.getActualSeconds() != null ? r.getActualSeconds() : 0)
                .sum();
        int weekCompletedCount = weekCompletedRecords.size();

        // 3. 历史累计完成会话
        List<PomodoroRecord> allCompletedRecords = pomodoroRecordMapper.selectList(
                new LambdaQueryWrapper<PomodoroRecord>()
                        .eq(PomodoroRecord::getUserId, userId)
                        .eq(PomodoroRecord::getSessionType, PomodoroSessionType.FOCUS.getCode())
                        .eq(PomodoroRecord::getStatus, PomodoroStatus.COMPLETED.getCode())
        );

        long totalFocusSeconds = allCompletedRecords.stream()
                .mapToLong(r -> r.getActualSeconds() != null ? r.getActualSeconds() : 0)
                .sum();
        long totalCompletedCount = allCompletedRecords.size();

        PomodoroSummaryVO summary = new PomodoroSummaryVO();
        summary.setTodayFocusSeconds(todayFocusSeconds);
        summary.setTodayFocusMinutes(Math.round(todayFocusSeconds / 60.0));
        summary.setTodayCompletedCount(todayCompletedCount);
        summary.setWeekFocusSeconds(weekFocusSeconds);
        summary.setWeekFocusMinutes(Math.round(weekFocusSeconds / 60.0));
        summary.setWeekCompletedCount(weekCompletedCount);
        summary.setTotalCompletedCount(totalCompletedCount);
        summary.setTotalFocusMinutes(Math.round(totalFocusSeconds / 60.0));

        return summary;
    }

    @Override
    public PomodoroSettingVO getSetting() {
        Long userId = SecurityUtil.getLoginUserId();
        PomodoroSetting setting = pomodoroSettingMapper.selectOne(
                new LambdaQueryWrapper<PomodoroSetting>().eq(PomodoroSetting::getUserId, userId)
        );

        if (setting == null) {
            return new PomodoroSettingVO(25, 5, 15, 4, false, false);
        }

        return new PomodoroSettingVO(
                setting.getFocusDuration(),
                setting.getShortBreakDuration(),
                setting.getLongBreakDuration(),
                setting.getLongBreakInterval(),
                setting.getAutoStartBreaks() != null && setting.getAutoStartBreaks() == 1,
                setting.getAutoStartPomodoros() != null && setting.getAutoStartPomodoros() == 1
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PomodoroSettingVO updateSetting(UpdatePomodoroSettingRequest request) {
        if (request == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "配置信息不能为空");
        }
        Long userId = SecurityUtil.getLoginUserId();

        PomodoroSetting setting = pomodoroSettingMapper.selectOne(
                new LambdaQueryWrapper<PomodoroSetting>().eq(PomodoroSetting::getUserId, userId)
        );

        if (setting == null) {
            setting = new PomodoroSetting();
            setting.setUserId(userId);
            setting.setFocusDuration(request.getFocusDuration() != null ? request.getFocusDuration() : 25);
            setting.setShortBreakDuration(request.getShortBreakDuration() != null ? request.getShortBreakDuration() : 5);
            setting.setLongBreakDuration(request.getLongBreakDuration() != null ? request.getLongBreakDuration() : 15);
            setting.setLongBreakInterval(request.getLongBreakInterval() != null ? request.getLongBreakInterval() : 4);
            setting.setAutoStartBreaks(Boolean.TRUE.equals(request.getAutoStartBreaks()) ? 1 : 0);
            setting.setAutoStartPomodoros(Boolean.TRUE.equals(request.getAutoStartPomodoros()) ? 1 : 0);
            pomodoroSettingMapper.insert(setting);
        } else {
            if (request.getFocusDuration() != null) {
                setting.setFocusDuration(request.getFocusDuration());
            }
            if (request.getShortBreakDuration() != null) {
                setting.setShortBreakDuration(request.getShortBreakDuration());
            }
            if (request.getLongBreakDuration() != null) {
                setting.setLongBreakDuration(request.getLongBreakDuration());
            }
            if (request.getLongBreakInterval() != null) {
                setting.setLongBreakInterval(request.getLongBreakInterval());
            }
            if (request.getAutoStartBreaks() != null) {
                setting.setAutoStartBreaks(request.getAutoStartBreaks() ? 1 : 0);
            }
            if (request.getAutoStartPomodoros() != null) {
                setting.setAutoStartPomodoros(request.getAutoStartPomodoros() ? 1 : 0);
            }
            pomodoroSettingMapper.updateById(setting);
        }

        moduleUsageLogService.logUsage(userId, "POMODORO", "UPDATE_SETTING", setting.getId());
        log.info("User {} updated pomodoro setting", userId);

        return new PomodoroSettingVO(
                setting.getFocusDuration(),
                setting.getShortBreakDuration(),
                setting.getLongBreakDuration(),
                setting.getLongBreakInterval(),
                setting.getAutoStartBreaks() != null && setting.getAutoStartBreaks() == 1,
                setting.getAutoStartPomodoros() != null && setting.getAutoStartPomodoros() == 1
        );
    }

    private PomodoroRecord findAndVerifyRecord(Long id, Long userId) {
        PomodoroRecord record = pomodoroRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "番茄钟会话不存在或无权访问");
        }
        return record;
    }

    private Map<Long, String> fetchGoalTitlesMap(List<PomodoroRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> goalIds = records.stream()
                .map(PomodoroRecord::getGoalId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (goalIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> map = new HashMap<>();
        List<Goal> goals = goalMapper.selectBatchIds(goalIds);
        for (Goal g : goals) {
            map.put(g.getId(), g.getTitle());
        }
        return map;
    }

    private Map<Long, String> fetchTodoTitlesMap(List<PomodoroRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> todoIds = records.stream()
                .map(PomodoroRecord::getTodoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (todoIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> map = new HashMap<>();
        List<TodoTask> todos = todoTaskMapper.selectBatchIds(todoIds);
        for (TodoTask t : todos) {
            map.put(t.getId(), t.getTitle());
        }
        return map;
    }

    private PomodoroRecordVO convertToVO(PomodoroRecord record, Map<Long, String> goalTitleMap, Map<Long, String> todoTitleMap) {
        if (record == null) {
            return null;
        }
        PomodoroRecordVO vo = new PomodoroRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setGoalId(record.getGoalId());
        if (record.getGoalId() != null) {
            vo.setGoalTitle(goalTitleMap.get(record.getGoalId()));
        }
        vo.setTodoId(record.getTodoId());
        if (record.getTodoId() != null) {
            vo.setTodoTitle(todoTitleMap.get(record.getTodoId()));
        }
        vo.setSessionType(record.getSessionType());
        PomodoroSessionType sessionType = PomodoroSessionType.fromCode(record.getSessionType());
        vo.setSessionTypeDesc(sessionType != null ? sessionType.getDesc() : record.getSessionType());

        vo.setPlannedSeconds(record.getPlannedSeconds());
        vo.setActualSeconds(record.getActualSeconds());
        vo.setStatus(record.getStatus());
        PomodoroStatus status = PomodoroStatus.fromCode(record.getStatus());
        vo.setStatusDesc(status != null ? status.getDesc() : record.getStatus());

        vo.setStartedAt(record.getStartedAt());
        vo.setEndedAt(record.getEndedAt());
        vo.setEndReason(record.getEndReason());
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }

    private PomodoroRecordVO convertSingle(PomodoroRecord record) {
        if (record == null) {
            return null;
        }
        List<PomodoroRecord> list = List.of(record);
        return convertToVO(record, fetchGoalTitlesMap(list), fetchTodoTitlesMap(list));
    }
}
