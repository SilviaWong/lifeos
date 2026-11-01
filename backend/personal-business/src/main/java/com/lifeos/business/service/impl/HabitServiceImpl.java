package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.dto.*;
import com.lifeos.business.entity.Habit;
import com.lifeos.business.entity.HabitCheckin;
import com.lifeos.business.enums.HabitFrequency;
import com.lifeos.business.enums.HabitStatus;
import com.lifeos.business.mapper.HabitCheckinMapper;
import com.lifeos.business.mapper.HabitMapper;
import com.lifeos.business.service.HabitService;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.vo.HabitCalendarVO;
import com.lifeos.business.vo.HabitCheckinVO;
import com.lifeos.business.vo.HabitSummaryVO;
import com.lifeos.business.vo.HabitVO;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.common.api.CommonErrorCode;
import com.lifeos.common.api.PageResult;
import com.lifeos.system.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 习惯打卡业务服务实现类
 */
@Service
public class HabitServiceImpl implements HabitService {

    private static final Logger log = LoggerFactory.getLogger(HabitServiceImpl.class);

    private final HabitMapper habitMapper;
    private final HabitCheckinMapper habitCheckinMapper;
    private final ModuleUsageLogService moduleUsageLogService;

    public HabitServiceImpl(HabitMapper habitMapper,
                            HabitCheckinMapper habitCheckinMapper,
                            ModuleUsageLogService moduleUsageLogService) {
        this.habitMapper = habitMapper;
        this.habitCheckinMapper = habitCheckinMapper;
        this.moduleUsageLogService = moduleUsageLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HabitVO createHabit(CreateHabitRequest request) {
        Long userId = SecurityUtil.getLoginUserId();

        validateDateRange(request.getStartDate(), request.getEndDate());

        HabitFrequency frequency = HabitFrequency.fromCode(request.getFrequency());
        if (frequency == null) {
            frequency = HabitFrequency.DAILY;
        }

        int targetCount = (request.getTargetCount() != null && request.getTargetCount() > 0)
                ? request.getTargetCount() : 1;

        LocalTime reminderTime = parseLocalTime(request.getReminderTime());

        Habit habit = new Habit();
        habit.setUserId(userId);
        habit.setName(request.getName().trim());
        habit.setDescription(request.getDescription());
        habit.setFrequency(frequency.getCode());
        habit.setTargetCount(targetCount);
        habit.setStartDate(request.getStartDate());
        habit.setEndDate(request.getEndDate());
        habit.setStatus(HabitStatus.ACTIVE.getCode());
        habit.setReminderTime(reminderTime);

        habitMapper.insert(habit);
        moduleUsageLogService.logUsage(userId, "HABIT", "CREATE", habit.getId());

        log.info("User {} created habit {} (id={})", userId, habit.getName(), habit.getId());
        return convertSingle(habit, Collections.emptyList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HabitVO updateHabit(Long id, UpdateHabitRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        Habit habit = getOwnedHabit(id, userId);

        validateDateRange(request.getStartDate(), request.getEndDate());

        HabitFrequency frequency = HabitFrequency.fromCode(request.getFrequency());
        if (frequency == null) {
            frequency = HabitFrequency.DAILY;
        }

        int targetCount = (request.getTargetCount() != null && request.getTargetCount() > 0)
                ? request.getTargetCount() : 1;

        habit.setName(request.getName().trim());
        habit.setDescription(request.getDescription());
        habit.setFrequency(frequency.getCode());
        habit.setTargetCount(targetCount);
        habit.setStartDate(request.getStartDate());
        habit.setEndDate(request.getEndDate());
        habit.setReminderTime(parseLocalTime(request.getReminderTime()));

        habitMapper.updateById(habit);
        moduleUsageLogService.logUsage(userId, "HABIT", "UPDATE", habit.getId());

        log.info("User {} updated habit {}", userId, habit.getId());
        List<HabitCheckin> checkins = habitCheckinMapper.selectList(
                new LambdaQueryWrapper<HabitCheckin>()
                        .eq(HabitCheckin::getHabitId, habit.getId())
                        .orderByAsc(HabitCheckin::getCheckinDate)
        );
        return convertSingle(habit, checkins);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HabitVO updateHabitStatus(Long id, UpdateHabitStatusRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        Habit habit = getOwnedHabit(id, userId);

        HabitStatus status = HabitStatus.fromCode(request.getStatus());
        if (status == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "无效的习惯状态");
        }

        habit.setStatus(status.getCode());
        habitMapper.updateById(habit);
        moduleUsageLogService.logUsage(userId, "HABIT", "UPDATE_STATUS_" + status.getCode(), habit.getId());

        log.info("User {} changed habit {} status to {}", userId, habit.getId(), status.getCode());
        List<HabitCheckin> checkins = habitCheckinMapper.selectList(
                new LambdaQueryWrapper<HabitCheckin>()
                        .eq(HabitCheckin::getHabitId, habit.getId())
                        .orderByAsc(HabitCheckin::getCheckinDate)
        );
        return convertSingle(habit, checkins);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHabit(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        Habit habit = getOwnedHabit(id, userId);

        habitMapper.deleteById(habit.getId());
        moduleUsageLogService.logUsage(userId, "HABIT", "DELETE", habit.getId());

        log.info("User {} deleted habit {}", userId, habit.getId());
    }

    @Override
    public HabitVO getHabitById(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        Habit habit = getOwnedHabit(id, userId);

        List<HabitCheckin> checkins = habitCheckinMapper.selectList(
                new LambdaQueryWrapper<HabitCheckin>()
                        .eq(HabitCheckin::getHabitId, habit.getId())
                        .orderByAsc(HabitCheckin::getCheckinDate)
        );
        return convertSingle(habit, checkins);
    }

    @Override
    public List<HabitVO> getHabitList(HabitQueryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();

        LambdaQueryWrapper<Habit> wrapper = new LambdaQueryWrapper<Habit>()
                .eq(Habit::getUserId, userId);

        if (request != null) {
            if (StringUtils.hasText(request.getStatus())) {
                wrapper.eq(Habit::getStatus, request.getStatus().trim().toUpperCase());
            }
            if (StringUtils.hasText(request.getFrequency())) {
                wrapper.eq(Habit::getFrequency, request.getFrequency().trim().toUpperCase());
            }
            if (StringUtils.hasText(request.getKeyword())) {
                wrapper.like(Habit::getName, request.getKeyword().trim());
            }
        }
        wrapper.orderByDesc(Habit::getCreatedAt);

        List<Habit> habits = habitMapper.selectList(wrapper);
        if (habits.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> habitIds = habits.stream().map(Habit::getId).collect(Collectors.toList());
        List<HabitCheckin> allCheckins = habitCheckinMapper.selectList(
                new LambdaQueryWrapper<HabitCheckin>()
                        .in(HabitCheckin::getHabitId, habitIds)
                        .orderByAsc(HabitCheckin::getCheckinDate)
        );

        Map<Long, List<HabitCheckin>> checkinMap = allCheckins.stream()
                .collect(Collectors.groupingBy(HabitCheckin::getHabitId));

        return habits.stream()
                .map(h -> convertSingle(h, checkinMap.getOrDefault(h.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HabitVO checkin(Long habitId, CheckinHabitRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        Habit habit = getOwnedHabit(habitId, userId);

        if (!HabitStatus.ACTIVE.getCode().equalsIgnoreCase(habit.getStatus())) {
            HabitStatus st = HabitStatus.fromCode(habit.getStatus());
            String desc = st != null ? st.getDesc() : habit.getStatus();
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "该习惯当前为「" + desc + "」状态，无法打卡");
        }

        LocalDate today = LocalDate.now();
        LocalDate checkinDate = (request != null && request.getCheckinDate() != null)
                ? request.getCheckinDate() : today;

        if (checkinDate.isAfter(today)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "不能在未来日期打卡");
        }
        if (checkinDate.isBefore(habit.getStartDate())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "打卡日期早于习惯开始日期(" + habit.getStartDate() + ")");
        }
        if (habit.getEndDate() != null && checkinDate.isAfter(habit.getEndDate())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "打卡日期晚于习惯结束日期(" + habit.getEndDate() + ")");
        }

        String note = (request != null && StringUtils.hasText(request.getNote())) ? request.getNote().trim() : null;
        int isMakeup = checkinDate.isBefore(today) ? 1 : 0;

        // 原生检查历史记录（含软删除），保证唯一约束幂等
        HabitCheckin rawRecord = habitCheckinMapper.findRawByHabitIdAndCheckinDate(habitId, checkinDate);
        if (rawRecord != null) {
            if (rawRecord.getDeleted() != null && rawRecord.getDeleted() == 0) {
                throw new BusinessException(CommonErrorCode.BAD_REQUEST, "该习惯在 " + checkinDate + " 已经打卡，请勿重复打卡");
            }
            // 重新激活软删除记录
            habitCheckinMapper.reactivateCheckin(rawRecord.getId(), note, isMakeup, LocalDateTime.now());
        } else {
            HabitCheckin checkin = new HabitCheckin();
            checkin.setUserId(userId);
            checkin.setHabitId(habitId);
            checkin.setCheckinDate(checkinDate);
            checkin.setNote(note);
            checkin.setIsMakeup(isMakeup);
            checkin.setCreatedAt(LocalDateTime.now());
            try {
                habitCheckinMapper.insert(checkin);
            } catch (DuplicateKeyException e) {
                log.warn("Duplicate checkin attempt for habit {} on date {}", habitId, checkinDate);
                throw new BusinessException(CommonErrorCode.BAD_REQUEST, "该习惯在 " + checkinDate + " 已经打卡，请勿重复打卡");
            }
        }

        moduleUsageLogService.logUsage(userId, "HABIT", "CHECKIN", habitId);
        log.info("User {} checked in habit {} for date {}", userId, habitId, checkinDate);

        List<HabitCheckin> checkins = habitCheckinMapper.selectList(
                new LambdaQueryWrapper<HabitCheckin>()
                        .eq(HabitCheckin::getHabitId, habit.getId())
                        .orderByAsc(HabitCheckin::getCheckinDate)
        );
        return convertSingle(habit, checkins);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HabitVO cancelCheckin(Long habitId, LocalDate checkinDate) {
        Long userId = SecurityUtil.getLoginUserId();
        Habit habit = getOwnedHabit(habitId, userId);

        LocalDate targetDate = (checkinDate != null) ? checkinDate : LocalDate.now();
        if (targetDate.isAfter(LocalDate.now())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "不能取消未来日期的打卡");
        }

        HabitCheckin record = habitCheckinMapper.selectOne(
                new LambdaQueryWrapper<HabitCheckin>()
                        .eq(HabitCheckin::getHabitId, habitId)
                        .eq(HabitCheckin::getCheckinDate, targetDate)
        );
        if (record == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "该习惯在 " + targetDate + " 尚未打卡或已取消");
        }

        habitCheckinMapper.deleteById(record.getId());
        moduleUsageLogService.logUsage(userId, "HABIT", "CANCEL_CHECKIN", habitId);
        log.info("User {} cancelled checkin for habit {} on date {}", userId, habitId, targetDate);

        List<HabitCheckin> checkins = habitCheckinMapper.selectList(
                new LambdaQueryWrapper<HabitCheckin>()
                        .eq(HabitCheckin::getHabitId, habit.getId())
                        .orderByAsc(HabitCheckin::getCheckinDate)
        );
        return convertSingle(habit, checkins);
    }

    @Override
    public HabitCalendarVO getHabitCalendar(Long habitId, Integer year, Integer month) {
        Long userId = SecurityUtil.getLoginUserId();
        Habit habit = getOwnedHabit(habitId, userId);

        LocalDate now = LocalDate.now();
        int queryYear = (year != null && year >= 2000 && year <= 2100) ? year : now.getYear();
        int queryMonth = (month != null && month >= 1 && month <= 12) ? month : now.getMonthValue();

        YearMonth ym = YearMonth.of(queryYear, queryMonth);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        List<HabitCheckin> allCheckins = habitCheckinMapper.selectList(
                new LambdaQueryWrapper<HabitCheckin>()
                        .eq(HabitCheckin::getHabitId, habitId)
                        .orderByAsc(HabitCheckin::getCheckinDate)
        );

        List<HabitCheckin> monthCheckins = allCheckins.stream()
                .filter(c -> !c.getCheckinDate().isBefore(monthStart) && !c.getCheckinDate().isAfter(monthEnd))
                .collect(Collectors.toList());

        List<LocalDate> checkinDates = monthCheckins.stream()
                .map(HabitCheckin::getCheckinDate)
                .sorted()
                .collect(Collectors.toList());

        List<HabitCheckinVO> checkinDetails = monthCheckins.stream()
                .map(c -> new HabitCheckinVO(
                        c.getId(),
                        c.getHabitId(),
                        habit.getName(),
                        c.getCheckinDate(),
                        c.getNote(),
                        c.getIsMakeup(),
                        c.getCreatedAt()
                ))
                .collect(Collectors.toList());

        // 计算月度完成率
        LocalDate rateStart = habit.getStartDate().isAfter(monthStart) ? habit.getStartDate() : monthStart;
        LocalDate rateEnd = (habit.getEndDate() != null && habit.getEndDate().isBefore(monthEnd))
                ? habit.getEndDate() : (monthEnd.isBefore(now) ? monthEnd : now);

        double monthRate = 0.0;
        if (!rateStart.isAfter(rateEnd)) {
            long expectedDays = ChronoUnit.DAYS.between(rateStart, rateEnd) + 1;
            if (expectedDays > 0) {
                long actualDays = checkinDates.stream()
                        .filter(d -> !d.isBefore(rateStart) && !d.isAfter(rateEnd))
                        .count();
                monthRate = Math.min(100.0, Math.round((actualDays * 100.0 / expectedDays) * 10.0) / 10.0);
            }
        }

        int currentStreak = calculateCurrentStreak(allCheckins, habit.getFrequency(), habit.getTargetCount());
        int maxStreak = calculateMaxStreak(allCheckins, habit.getFrequency(), habit.getTargetCount());

        HabitCalendarVO vo = new HabitCalendarVO();
        vo.setHabitId(habit.getId());
        vo.setHabitName(habit.getName());
        vo.setYear(queryYear);
        vo.setMonth(queryMonth);
        vo.setDaysInMonth(ym.lengthOfMonth());
        vo.setMonthCheckins(checkinDates.size());
        vo.setMonthCompletionRate(monthRate);
        vo.setCurrentStreak(currentStreak);
        vo.setMaxStreak(maxStreak);
        vo.setTotalCheckins(allCheckins.size());
        vo.setCheckinDates(checkinDates);
        vo.setCheckinDetails(checkinDetails);

        return vo;
    }

    @Override
    public PageResult<HabitCheckinVO> getHabitCheckins(Long habitId, Integer pageNo, Integer pageSize) {
        Long userId = SecurityUtil.getLoginUserId();
        Habit habit = getOwnedHabit(habitId, userId);

        int current = (pageNo != null && pageNo > 0) ? pageNo : 1;
        int size = (pageSize != null && pageSize > 0 && pageSize <= 100) ? pageSize : 20;

        Page<HabitCheckin> page = new Page<>(current, size);
        Page<HabitCheckin> resultPage = habitCheckinMapper.selectPage(
                page,
                new LambdaQueryWrapper<HabitCheckin>()
                        .eq(HabitCheckin::getHabitId, habitId)
                        .orderByDesc(HabitCheckin::getCheckinDate)
        );

        List<HabitCheckinVO> voList = resultPage.getRecords().stream()
                .map(c -> new HabitCheckinVO(
                        c.getId(),
                        c.getHabitId(),
                        habit.getName(),
                        c.getCheckinDate(),
                        c.getNote(),
                        c.getIsMakeup(),
                        c.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new PageResult<>(voList, resultPage.getTotal(), current, size);
    }

    @Override
    public HabitSummaryVO getHabitSummary() {
        Long userId = SecurityUtil.getLoginUserId();

        List<Habit> habits = habitMapper.selectList(
                new LambdaQueryWrapper<Habit>().eq(Habit::getUserId, userId)
        );
        if (habits.isEmpty()) {
            return new HabitSummaryVO(0, 0, 0, 0.0);
        }

        long totalHabits = habits.size();
        long activeHabits = habits.stream()
                .filter(h -> HabitStatus.ACTIVE.getCode().equalsIgnoreCase(h.getStatus()))
                .count();

        LocalDate today = LocalDate.now();
        Long todayCheckedHabits = habitCheckinMapper.selectCount(
                new LambdaQueryWrapper<HabitCheckin>()
                        .eq(HabitCheckin::getUserId, userId)
                        .eq(HabitCheckin::getCheckinDate, today)
        );

        List<Long> habitIds = habits.stream().map(Habit::getId).collect(Collectors.toList());
        List<HabitCheckin> allCheckins = habitCheckinMapper.selectList(
                new LambdaQueryWrapper<HabitCheckin>()
                        .in(HabitCheckin::getHabitId, habitIds)
                        .orderByAsc(HabitCheckin::getCheckinDate)
        );

        Map<Long, List<HabitCheckin>> checkinMap = allCheckins.stream()
                .collect(Collectors.groupingBy(HabitCheckin::getHabitId));

        double sumRate = 0.0;
        int validCount = 0;
        for (Habit habit : habits) {
            double rate = calculateCompletionRate(habit, checkinMap.getOrDefault(habit.getId(), Collections.emptyList()));
            sumRate += rate;
            validCount++;
        }

        double overallCompletionRate = validCount > 0
                ? Math.round((sumRate / validCount) * 10.0) / 10.0
                : 0.0;

        return new HabitSummaryVO(totalHabits, activeHabits, todayCheckedHabits, overallCompletionRate);
    }

    @Override
    public long countTodayCheckins(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Long count = habitCheckinMapper.selectCount(
                new LambdaQueryWrapper<HabitCheckin>()
                        .eq(HabitCheckin::getUserId, userId)
                        .eq(HabitCheckin::getCheckinDate, LocalDate.now())
        );
        return count != null ? count : 0L;
    }

    // ================= 私有工具与计算方法 =================

    private Habit getOwnedHabit(Long id, Long userId) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "习惯ID不能为空");
        }
        Habit habit = habitMapper.selectById(id);
        if (habit == null || !habit.getUserId().equals(userId)) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "习惯不存在或无权访问");
        }
        return habit;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "开始日期不能为空");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "结束日期不能早于开始日期");
        }
    }

    private LocalTime parseLocalTime(String reminderTimeStr) {
        if (!StringUtils.hasText(reminderTimeStr)) {
            return null;
        }
        try {
            String trimmed = reminderTimeStr.trim();
            if (trimmed.length() == 5) { // HH:mm
                return LocalTime.parse(trimmed, DateTimeFormatter.ofPattern("HH:mm"));
            }
            return LocalTime.parse(trimmed, DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (Exception e) {
            log.warn("Failed to parse reminder time: {}", reminderTimeStr);
            return null;
        }
    }

    private HabitVO convertSingle(Habit habit, List<HabitCheckin> checkins) {
        HabitVO vo = new HabitVO();
        vo.setId(habit.getId());
        vo.setName(habit.getName());
        vo.setDescription(habit.getDescription());
        vo.setFrequency(habit.getFrequency());

        HabitFrequency freq = HabitFrequency.fromCode(habit.getFrequency());
        vo.setFrequencyDesc(freq != null ? freq.getDesc() : habit.getFrequency());

        vo.setTargetCount(habit.getTargetCount());
        vo.setStartDate(habit.getStartDate());
        vo.setEndDate(habit.getEndDate());
        vo.setStatus(habit.getStatus());

        HabitStatus st = HabitStatus.fromCode(habit.getStatus());
        vo.setStatusDesc(st != null ? st.getDesc() : habit.getStatus());

        vo.setReminderTime(habit.getReminderTime());
        vo.setCreatedAt(habit.getCreatedAt());
        vo.setUpdatedAt(habit.getUpdatedAt());

        // 计算打卡指标
        LocalDate today = LocalDate.now();
        boolean todayChecked = checkins.stream()
                .anyMatch(c -> c.getCheckinDate().equals(today));
        vo.setTodayChecked(todayChecked);

        int currentStreak = calculateCurrentStreak(checkins, habit.getFrequency(), habit.getTargetCount());
        int maxStreak = calculateMaxStreak(checkins, habit.getFrequency(), habit.getTargetCount());
        double completionRate = calculateCompletionRate(habit, checkins);

        vo.setCurrentStreak(currentStreak);
        vo.setMaxStreak(maxStreak);
        vo.setTotalCheckins(checkins.size());
        vo.setCompletionRate(completionRate);

        return vo;
    }

    /**
     * 计算当前连续打卡数（支持跨月跨年和周频率）
     */
    private int calculateCurrentStreak(List<HabitCheckin> checkins, String frequency, Integer targetCount) {
        if (checkins == null || checkins.isEmpty()) {
            return 0;
        }

        if (HabitFrequency.WEEKLY.getCode().equalsIgnoreCase(frequency)) {
            return calculateWeeklyCurrentStreak(checkins, targetCount != null ? targetCount : 1);
        }

        // DAILY 每日频率连续打卡天数计算
        Set<LocalDate> dateSet = checkins.stream()
                .map(HabitCheckin::getCheckinDate)
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate cursor = today;

        if (dateSet.contains(cursor)) {
            // 今日已打卡，从今日倒推
            while (dateSet.contains(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            }
        } else {
            // 今日尚未打卡，但若昨日已打卡，连续打卡尚未中断
            cursor = cursor.minusDays(1);
            while (dateSet.contains(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            }
        }
        return streak;
    }

    /**
     * 计算历史最大连续打卡数
     */
    private int calculateMaxStreak(List<HabitCheckin> checkins, String frequency, Integer targetCount) {
        if (checkins == null || checkins.isEmpty()) {
            return 0;
        }

        if (HabitFrequency.WEEKLY.getCode().equalsIgnoreCase(frequency)) {
            return calculateWeeklyMaxStreak(checkins, targetCount != null ? targetCount : 1);
        }

        List<LocalDate> sortedDates = checkins.stream()
                .map(HabitCheckin::getCheckinDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (sortedDates.isEmpty()) {
            return 0;
        }

        int maxStreak = 1;
        int currentRun = 1;

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate prev = sortedDates.get(i - 1);
            LocalDate curr = sortedDates.get(i);

            if (curr.equals(prev.plusDays(1))) {
                currentRun++;
                if (currentRun > maxStreak) {
                    maxStreak = currentRun;
                }
            } else if (!curr.equals(prev)) {
                currentRun = 1;
            }
        }
        return maxStreak;
    }

    /**
     * 周打卡连续周数计算
     */
    private int calculateWeeklyCurrentStreak(List<HabitCheckin> checkins, int targetCount) {
        WeekFields weekFields = WeekFields.ISO;
        LocalDate today = LocalDate.now();

        // 按 周基准年-第几周 分组统计打卡次数
        Map<String, Long> weekCountMap = checkins.stream().collect(
                Collectors.groupingBy(
                        c -> c.getCheckinDate().get(weekFields.weekBasedYear()) + "_" + c.getCheckinDate().get(weekFields.weekOfWeekBasedYear()),
                        Collectors.counting()
                )
        );

        int currentStreak = 0;
        LocalDate cursor = today;
        String thisWeekKey = cursor.get(weekFields.weekBasedYear()) + "_" + cursor.get(weekFields.weekOfWeekBasedYear());

        boolean thisWeekCompleted = weekCountMap.getOrDefault(thisWeekKey, 0L) >= targetCount;
        if (thisWeekCompleted) {
            while (true) {
                String key = cursor.get(weekFields.weekBasedYear()) + "_" + cursor.get(weekFields.weekOfWeekBasedYear());
                if (weekCountMap.getOrDefault(key, 0L) >= targetCount) {
                    currentStreak++;
                    cursor = cursor.minusWeeks(1);
                } else {
                    break;
                }
            }
        } else {
            // 本周尚未达标，检查上周是否达标
            cursor = cursor.minusWeeks(1);
            while (true) {
                String key = cursor.get(weekFields.weekBasedYear()) + "_" + cursor.get(weekFields.weekOfWeekBasedYear());
                if (weekCountMap.getOrDefault(key, 0L) >= targetCount) {
                    currentStreak++;
                    cursor = cursor.minusWeeks(1);
                } else {
                    break;
                }
            }
        }
        return currentStreak;
    }

    private int calculateWeeklyMaxStreak(List<HabitCheckin> checkins, int targetCount) {
        WeekFields weekFields = WeekFields.ISO;
        List<LocalDate> dates = checkins.stream()
                .map(HabitCheckin::getCheckinDate)
                .sorted()
                .collect(Collectors.toList());

        if (dates.isEmpty()) {
            return 0;
        }

        // 识别出达标的连续周
        Map<String, Long> weekCountMap = dates.stream().collect(
                Collectors.groupingBy(
                        d -> d.get(weekFields.weekBasedYear()) + "_" + d.get(weekFields.weekOfWeekBasedYear()),
                        Collectors.counting()
                )
        );

        LocalDate cursor = dates.get(0);
        LocalDate maxDate = dates.get(dates.size() - 1);

        int maxStreak = 0;
        int currentRun = 0;

        while (!cursor.isAfter(maxDate.plusWeeks(1))) {
            String key = cursor.get(weekFields.weekBasedYear()) + "_" + cursor.get(weekFields.weekOfWeekBasedYear());
            if (weekCountMap.getOrDefault(key, 0L) >= targetCount) {
                currentRun++;
                if (currentRun > maxStreak) {
                    maxStreak = currentRun;
                }
            } else {
                currentRun = 0;
            }
            cursor = cursor.plusWeeks(1);
        }
        return maxStreak;
    }

    /**
     * 计算完成率百分比
     */
    private double calculateCompletionRate(Habit habit, List<HabitCheckin> checkins) {
        LocalDate today = LocalDate.now();
        LocalDate start = habit.getStartDate();
        LocalDate end = (habit.getEndDate() != null && habit.getEndDate().isBefore(today))
                ? habit.getEndDate() : today;

        if (start.isAfter(today)) {
            return 0.0;
        }

        long expectedDays = ChronoUnit.DAYS.between(start, end) + 1;
        if (expectedDays <= 0) {
            return 0.0;
        }

        if (HabitFrequency.WEEKLY.getCode().equalsIgnoreCase(habit.getFrequency())) {
            long expectedWeeks = Math.max(1, (expectedDays + 6) / 7);
            int target = (habit.getTargetCount() != null && habit.getTargetCount() > 0) ? habit.getTargetCount() : 1;
            long expectedTotal = expectedWeeks * target;

            long actualCheckins = checkins.stream()
                    .filter(c -> !c.getCheckinDate().isBefore(start) && !c.getCheckinDate().isAfter(end))
                    .count();

            double rate = (actualCheckins * 100.0) / expectedTotal;
            return Math.min(100.0, Math.round(rate * 10.0) / 10.0);
        }

        // DAILY
        long actualCheckins = checkins.stream()
                .filter(c -> !c.getCheckinDate().isBefore(start) && !c.getCheckinDate().isAfter(end))
                .map(HabitCheckin::getCheckinDate)
                .distinct()
                .count();

        double rate = (actualCheckins * 100.0) / expectedDays;
        return Math.min(100.0, Math.round(rate * 10.0) / 10.0);
    }
}
