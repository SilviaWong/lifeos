package com.lifeos.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lifeos.business.entity.FinanceRecord;
import com.lifeos.business.entity.HealthRecord;
import com.lifeos.business.entity.ModuleUsageLog;
import com.lifeos.business.entity.PomodoroRecord;
import com.lifeos.business.entity.TodoTask;
import com.lifeos.business.entity.HabitCheckin;
import com.lifeos.business.mapper.FinanceRecordMapper;
import com.lifeos.business.mapper.HealthRecordMapper;
import com.lifeos.business.mapper.ModuleUsageLogMapper;
import com.lifeos.business.mapper.PomodoroRecordMapper;
import com.lifeos.business.mapper.TodoTaskMapper;
import com.lifeos.business.mapper.HabitCheckinMapper;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.statistics.dto.AdminDashboardQueryRequest;
import com.lifeos.statistics.service.AdminDashboardService;
import com.lifeos.statistics.vo.AdminDashboardOverviewVO;
import com.lifeos.statistics.vo.AdminModuleTrendVO;
import com.lifeos.statistics.vo.DashboardCardMetricsVO;
import com.lifeos.statistics.vo.DateRangeFilterVO;
import com.lifeos.statistics.vo.FinanceStatSummaryVO;
import com.lifeos.statistics.vo.HealthTypeStatVO;
import com.lifeos.statistics.vo.ModuleDailyStatVO;
import com.lifeos.statistics.vo.ModuleUsageRankVO;
import com.lifeos.statistics.vo.UserTrendPointVO;
import com.lifeos.system.entity.SysUser;
import com.lifeos.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端综合数据看板与运营统计服务实现
 */
@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final SysUserMapper sysUserMapper;
    private final ModuleUsageLogMapper moduleUsageLogMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final FinanceRecordMapper financeRecordMapper;
    private final PomodoroRecordMapper pomodoroRecordMapper;
    private final TodoTaskMapper todoTaskMapper;
    private final HabitCheckinMapper habitCheckinMapper;

    public AdminDashboardServiceImpl(SysUserMapper sysUserMapper,
                                     ModuleUsageLogMapper moduleUsageLogMapper,
                                     HealthRecordMapper healthRecordMapper,
                                     FinanceRecordMapper financeRecordMapper,
                                     PomodoroRecordMapper pomodoroRecordMapper,
                                     TodoTaskMapper todoTaskMapper,
                                     HabitCheckinMapper habitCheckinMapper) {
        this.sysUserMapper = sysUserMapper;
        this.moduleUsageLogMapper = moduleUsageLogMapper;
        this.healthRecordMapper = healthRecordMapper;
        this.financeRecordMapper = financeRecordMapper;
        this.pomodoroRecordMapper = pomodoroRecordMapper;
        this.todoTaskMapper = todoTaskMapper;
        this.habitCheckinMapper = habitCheckinMapper;
    }

    @Override
    public AdminDashboardOverviewVO getDashboardOverview(AdminDashboardQueryRequest request) {
        DateRangeHolder range = resolveDateRange(request);

        AdminDashboardOverviewVO overview = new AdminDashboardOverviewVO();
        overview.setFilter(new DateRangeFilterVO(range.startDate, range.endDate, range.dayCount));

        // 1. 核心卡片指标（累计 vs 区间）
        overview.setOverviewCards(buildCardMetrics(range));

        // 2. 用户趋势（每日新增注册用户与每日活跃用户）
        overview.setUserTrend(buildUserTrends(range));

        // 3. 业务模块使用排行
        overview.setModuleUsageRank(buildModuleRanking(range));

        // 4. 健康记录类型分布
        overview.setHealthDistribution(buildHealthDistribution(range));

        // 5. 财务收支概览
        overview.setFinanceSummary(buildFinanceSummary(range));

        return overview;
    }

    @Override
    public AdminModuleTrendVO getModuleTrends(AdminDashboardQueryRequest request) {
        DateRangeHolder range = resolveDateRange(request);

        List<String> dates = new ArrayList<>();
        LocalDate cur = range.startDate;
        while (!cur.isAfter(range.endDate)) {
            dates.add(cur.toString());
            cur = cur.plusDays(1);
        }

        // 查询各模块在每天的操作频次
        List<Map<String, Object>> records = moduleUsageLogMapper.selectMaps(
                new QueryWrapper<ModuleUsageLog>()
                        .select("module_code, cast(occurred_at as date) as log_date, count(*) as cnt")
                        .ge("occurred_at", range.startDateTime)
                        .le("occurred_at", range.endDateTime)
                        .groupBy("module_code, cast(occurred_at as date)")
        );

        // Map<moduleCode, Map<dateStr, count>>
        Map<String, Map<String, Long>> moduleDateCountMap = new HashMap<>();
        for (Map<String, Object> row : records) {
            String moduleCode = String.valueOf(getMapValue(row, "module_code"));
            LocalDate logDate = parseLocalDate(getMapValue(row, "log_date"));
            Long cnt = parseLong(getMapValue(row, "cnt"));
            if (moduleCode != null && logDate != null) {
                moduleDateCountMap.computeIfAbsent(moduleCode, k -> new HashMap<>())
                        .put(logDate.toString(), cnt);
            }
        }

        List<String> targetModules = Arrays.asList("TODO", "POMODORO", "HABIT", "HEALTH", "FINANCE", "GOAL");
        List<ModuleDailyStatVO> seriesList = new ArrayList<>();
        for (String mod : targetModules) {
            Map<String, Long> dateMap = moduleDateCountMap.getOrDefault(mod, new HashMap<>());
            List<Long> countList = new ArrayList<>();
            for (String d : dates) {
                countList.add(dateMap.getOrDefault(d, 0L));
            }
            seriesList.add(new ModuleDailyStatVO(mod, getModuleName(mod), countList));
        }

        return new AdminModuleTrendVO(dates, seriesList);
    }

    private DashboardCardMetricsVO buildCardMetrics(DateRangeHolder range) {
        DashboardCardMetricsVO cards = new DashboardCardMetricsVO();

        // 累计总用户数 (sys_user)
        cards.setTotalUserCount(sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeleted, 0)
        ));

        // 时间区间内新增用户数
        cards.setRangeNewUserCount(sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0)
                        .ge(SysUser::getCreatedAt, range.startDateTime)
                        .le(SysUser::getCreatedAt, range.endDateTime)
        ));

        // 时间区间内活跃用户数 (有操作记录的去重用户数)
        List<Map<String, Object>> actList = moduleUsageLogMapper.selectMaps(
                new QueryWrapper<ModuleUsageLog>()
                        .select("count(distinct user_id) as act_users")
                        .ge("occurred_at", range.startDateTime)
                        .le("occurred_at", range.endDateTime)
        );
        cards.setRangeActiveUserCount(actList.isEmpty() ? 0L : parseLong(getMapValue(actList.get(0), "act_users")));

        // 今日活跃用户数 (DAU)
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        List<Map<String, Object>> todayActList = moduleUsageLogMapper.selectMaps(
                new QueryWrapper<ModuleUsageLog>()
                        .select("count(distinct user_id) as act_users")
                        .ge("occurred_at", todayStart)
                        .le("occurred_at", todayEnd)
        );
        cards.setTodayActiveUserCount(todayActList.isEmpty() ? 0L : parseLong(getMapValue(todayActList.get(0), "act_users")));

        // 健康记录总数 (累计 vs 区间)
        cards.setTotalHealthRecordCount(healthRecordMapper.selectCount(
                new LambdaQueryWrapper<HealthRecord>().eq(HealthRecord::getDeleted, 0)
        ));
        cards.setRangeHealthRecordCount(healthRecordMapper.selectCount(
                new LambdaQueryWrapper<HealthRecord>()
                        .eq(HealthRecord::getDeleted, 0)
                        .ge(HealthRecord::getRecordTime, range.startDateTime)
                        .le(HealthRecord::getRecordTime, range.endDateTime)
        ));

        // 财务累计数据
        List<Map<String, Object>> totalFinanceList = financeRecordMapper.selectMaps(
                new QueryWrapper<FinanceRecord>()
                        .select("record_type, sum(amount) as sum_amount")
                        .eq("deleted", 0)
                        .groupBy("record_type")
        );
        for (Map<String, Object> map : totalFinanceList) {
            String type = String.valueOf(getMapValue(map, "record_type"));
            BigDecimal sum = parseBigDecimal(getMapValue(map, "sum_amount"));
            if ("EXPENSE".equalsIgnoreCase(type)) {
                cards.setTotalExpenseAmount(sum);
            } else if ("INCOME".equalsIgnoreCase(type)) {
                cards.setTotalIncomeAmount(sum);
            }
        }

        // 财务区间数据
        List<Map<String, Object>> rangeFinanceList = financeRecordMapper.selectMaps(
                new QueryWrapper<FinanceRecord>()
                        .select("record_type, sum(amount) as sum_amount")
                        .eq("deleted", 0)
                        .ge("record_date", range.startDate)
                        .le("record_date", range.endDate)
                        .groupBy("record_type")
        );
        BigDecimal rangeExpense = BigDecimal.ZERO;
        BigDecimal rangeIncome = BigDecimal.ZERO;
        for (Map<String, Object> map : rangeFinanceList) {
            String type = String.valueOf(getMapValue(map, "record_type"));
            BigDecimal sum = parseBigDecimal(getMapValue(map, "sum_amount"));
            if ("EXPENSE".equalsIgnoreCase(type)) {
                rangeExpense = sum;
            } else if ("INCOME".equalsIgnoreCase(type)) {
                rangeIncome = sum;
            }
        }
        cards.setRangeExpenseAmount(rangeExpense);
        cards.setRangeIncomeAmount(rangeIncome);
        cards.setRangeNetBalance(rangeIncome.subtract(rangeExpense));

        // 专注时长 (累计 vs 区间)
        List<Map<String, Object>> totalPomodoroList = pomodoroRecordMapper.selectMaps(
                new QueryWrapper<PomodoroRecord>()
                        .select("sum(actual_seconds) as total_seconds")
                        .eq("status", "COMPLETED")
        );
        long totalSecs = totalPomodoroList.isEmpty() ? 0L : parseLong(getMapValue(totalPomodoroList.get(0), "total_seconds"));
        cards.setTotalFocusMinutes(totalSecs / 60);

        List<Map<String, Object>> rangePomodoroList = pomodoroRecordMapper.selectMaps(
                new QueryWrapper<PomodoroRecord>()
                        .select("sum(actual_seconds) as total_seconds")
                        .eq("status", "COMPLETED")
                        .ge("started_at", range.startDateTime)
                        .le("started_at", range.endDateTime)
        );
        long rangeSecs = rangePomodoroList.isEmpty() ? 0L : parseLong(getMapValue(rangePomodoroList.get(0), "total_seconds"));
        cards.setRangeFocusMinutes(rangeSecs / 60);

        // 待办任务累计与完成数
        cards.setTotalTodoCount(todoTaskMapper.selectCount(
                new LambdaQueryWrapper<TodoTask>().eq(TodoTask::getDeleted, 0)
        ));
        cards.setCompletedTodoCount(todoTaskMapper.selectCount(
                new LambdaQueryWrapper<TodoTask>()
                        .eq(TodoTask::getDeleted, 0)
                        .eq(TodoTask::getStatus, "DONE")
        ));

        // 习惯打卡累计数
        cards.setTotalHabitCheckinCount(habitCheckinMapper.selectCount(
                new LambdaQueryWrapper<HabitCheckin>().eq(HabitCheckin::getDeleted, 0)
        ));

        return cards;
    }

    private List<UserTrendPointVO> buildUserTrends(DateRangeHolder range) {
        // 查询每日新增用户
        List<Map<String, Object>> newUsersList = sysUserMapper.selectMaps(
                new QueryWrapper<SysUser>()
                        .select("cast(created_at as date) as reg_date, count(*) as cnt")
                        .eq("deleted", 0)
                        .ge("created_at", range.startDateTime)
                        .le("created_at", range.endDateTime)
                        .groupBy("cast(created_at as date)")
        );
        Map<LocalDate, Long> newUsersMap = new HashMap<>();
        for (Map<String, Object> map : newUsersList) {
            LocalDate d = parseLocalDate(getMapValue(map, "reg_date"));
            if (d != null) {
                newUsersMap.put(d, parseLong(getMapValue(map, "cnt")));
            }
        }

        // 查询每日活跃用户 (DAU)
        List<Map<String, Object>> actUsersList = moduleUsageLogMapper.selectMaps(
                new QueryWrapper<ModuleUsageLog>()
                        .select("cast(occurred_at as date) as act_date, count(distinct user_id) as cnt")
                        .ge("occurred_at", range.startDateTime)
                        .le("occurred_at", range.endDateTime)
                        .groupBy("cast(occurred_at as date)")
        );
        Map<LocalDate, Long> actUsersMap = new HashMap<>();
        for (Map<String, Object> map : actUsersList) {
            LocalDate d = parseLocalDate(getMapValue(map, "act_date"));
            if (d != null) {
                actUsersMap.put(d, parseLong(getMapValue(map, "cnt")));
            }
        }

        List<UserTrendPointVO> points = new ArrayList<>();
        LocalDate cur = range.startDate;
        while (!cur.isAfter(range.endDate)) {
            points.add(new UserTrendPointVO(
                    cur,
                    newUsersMap.getOrDefault(cur, 0L),
                    actUsersMap.getOrDefault(cur, 0L)
            ));
            cur = cur.plusDays(1);
        }

        return points;
    }

    private List<ModuleUsageRankVO> buildModuleRanking(DateRangeHolder range) {
        List<Map<String, Object>> rankList = moduleUsageLogMapper.selectMaps(
                new QueryWrapper<ModuleUsageLog>()
                        .select("module_code, count(*) as cnt, count(distinct user_id) as users")
                        .ge("occurred_at", range.startDateTime)
                        .le("occurred_at", range.endDateTime)
                        .groupBy("module_code")
                        .orderByDesc("cnt")
        );

        long totalActions = 0L;
        for (Map<String, Object> map : rankList) {
            totalActions += parseLong(getMapValue(map, "cnt"));
        }

        List<ModuleUsageRankVO> result = new ArrayList<>();
        for (Map<String, Object> map : rankList) {
            String code = String.valueOf(getMapValue(map, "module_code"));
            long cnt = parseLong(getMapValue(map, "cnt"));
            long users = parseLong(getMapValue(map, "users"));
            double pct = 0.0;
            if (totalActions > 0) {
                pct = BigDecimal.valueOf(cnt)
                        .divide(BigDecimal.valueOf(totalActions), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();
            }
            result.add(new ModuleUsageRankVO(code, getModuleName(code), cnt, users, pct));
        }
        return result;
    }

    private List<HealthTypeStatVO> buildHealthDistribution(DateRangeHolder range) {
        List<Map<String, Object>> list = healthRecordMapper.selectMaps(
                new QueryWrapper<HealthRecord>()
                        .select("metric_code, count(*) as cnt")
                        .eq("deleted", 0)
                        .ge("record_time", range.startDateTime)
                        .le("record_time", range.endDateTime)
                        .groupBy("metric_code")
                        .orderByDesc("cnt")
        );

        Map<String, String> nameMap = new LinkedHashMap<>();
        nameMap.put("STEPS", "步数");
        nameMap.put("SLEEP_HOURS", "睡眠");
        nameMap.put("WEIGHT", "体重");
        nameMap.put("WATER_ML", "饮水");
        nameMap.put("CALORIES", "饮食卡路里");

        Map<String, Long> countMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            String type = String.valueOf(getMapValue(map, "metric_code"));
            countMap.put(type, parseLong(getMapValue(map, "cnt")));
        }

        List<HealthTypeStatVO> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : nameMap.entrySet()) {
            result.add(new HealthTypeStatVO(
                    entry.getKey(),
                    entry.getValue(),
                    countMap.getOrDefault(entry.getKey(), 0L)
            ));
        }
        return result;
    }

    private FinanceStatSummaryVO buildFinanceSummary(DateRangeHolder range) {
        List<Map<String, Object>> list = financeRecordMapper.selectMaps(
                new QueryWrapper<FinanceRecord>()
                        .select("record_type, sum(amount) as sum_amount, count(*) as cnt")
                        .eq("deleted", 0)
                        .ge("record_date", range.startDate)
                        .le("record_date", range.endDate)
                        .groupBy("record_type")
        );

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        long recordCount = 0L;

        for (Map<String, Object> map : list) {
            String type = String.valueOf(getMapValue(map, "record_type"));
            BigDecimal sum = parseBigDecimal(getMapValue(map, "sum_amount"));
            long cnt = parseLong(getMapValue(map, "cnt"));
            recordCount += cnt;

            if ("INCOME".equalsIgnoreCase(type)) {
                income = sum;
            } else if ("EXPENSE".equalsIgnoreCase(type)) {
                expense = sum;
            }
        }

        FinanceStatSummaryVO vo = new FinanceStatSummaryVO();
        vo.setTotalIncome(income);
        vo.setTotalExpense(expense);
        vo.setNetBalance(income.subtract(expense));
        vo.setRecordCount(recordCount);
        return vo;
    }

    private DateRangeHolder resolveDateRange(AdminDashboardQueryRequest request) {
        LocalDate endDate = request != null && request.getEndDate() != null
                ? request.getEndDate()
                : LocalDate.now();
        LocalDate startDate = request != null && request.getStartDate() != null
                ? request.getStartDate()
                : endDate.minusDays(6);

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days > 90) {
            throw new BusinessException(400, "统计时间范围不能超过90天");
        }

        return new DateRangeHolder(startDate, endDate, days);
    }

    private String getModuleName(String code) {
        if (code == null) return "未知模块";
        return switch (code.toUpperCase()) {
            case "TODO" -> "待办清单";
            case "GOAL" -> "目标与计划";
            case "HEALTH" -> "健康记录";
            case "POMODORO" -> "番茄专注";
            case "HABIT" -> "习惯打卡";
            case "FINANCE" -> "收支记账";
            default -> code;
        };
    }

    private Object getMapValue(Map<String, Object> map, String key) {
        if (map == null || key == null) return null;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Long parseLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    private BigDecimal parseBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (val instanceof BigDecimal) return ((BigDecimal) val).setScale(2, RoundingMode.HALF_UP);
        try {
            return new BigDecimal(val.toString()).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    private LocalDate parseLocalDate(Object val) {
        if (val == null) return null;
        if (val instanceof LocalDate) return (LocalDate) val;
        if (val instanceof java.sql.Date) return ((java.sql.Date) val).toLocalDate();
        try {
            return LocalDate.parse(val.toString().substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    private static class DateRangeHolder {
        final LocalDate startDate;
        final LocalDate endDate;
        final long dayCount;
        final LocalDateTime startDateTime;
        final LocalDateTime endDateTime;

        DateRangeHolder(LocalDate startDate, LocalDate endDate, long dayCount) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.dayCount = dayCount;
            this.startDateTime = startDate.atStartOfDay();
            this.endDateTime = endDate.atTime(LocalTime.MAX);
        }
    }
}
