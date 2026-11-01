package com.lifeos.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.lifeos.system.dto.LoginDTO;
import com.lifeos.system.dto.RegisterDTO;
import com.lifeos.system.entity.SysRole;
import com.lifeos.system.entity.SysUser;
import com.lifeos.system.entity.SysUserRole;
import com.lifeos.system.mapper.SysRoleMapper;
import com.lifeos.system.mapper.SysUserMapper;
import com.lifeos.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("管理端数据看板、活跃指标与模块使用统计集成测试")
class AdminDashboardAndStatisticsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModuleUsageLogMapper moduleUsageLogMapper;

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Autowired
    private FinanceRecordMapper financeRecordMapper;

    @Autowired
    private PomodoroRecordMapper pomodoroRecordMapper;

    @Autowired
    private TodoTaskMapper todoTaskMapper;

    @Autowired
    private HabitCheckinMapper habitCheckinMapper;

    @BeforeEach
    void setUp() {
        moduleUsageLogMapper.delete(null);
        healthRecordMapper.delete(null);
        financeRecordMapper.delete(null);
        pomodoroRecordMapper.delete(null);
        todoTaskMapper.delete(null);
        habitCheckinMapper.delete(null);
    }

    private String registerAndGetToken(String username, String password) throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(username);
        registerDTO.setPassword(password);
        registerDTO.setPhone("138" + System.currentTimeMillis() % 100000000);
        registerDTO.setNickname("普通用户_" + username);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDTO(username, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    private String createAdminUserAndGetToken(String username, String password) throws Exception {
        SysUser admin = new SysUser();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setNickname("看板超级管理员");
        admin.setStatus(SysUser.STATUS_ENABLED);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        admin.setDeleted(0);
        sysUserMapper.insert(admin);

        SysRole adminRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, SysRole.ROLE_ADMIN)
        );
        sysUserRoleMapper.insert(new SysUserRole(admin.getId(), adminRole.getId()));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDTO(username, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    @Test
    @DisplayName("测试普通用户越权拦截：普通用户不能访问 /api/admin/dashboard 和 /api/admin/operations 接口")
    void testNormalUserAccessAdminDashboardForbidden() throws Exception {
        String userToken = registerAndGetToken("normal_stat_user", "User123456");

        // 1. 普通用户访问看板概览接口 -> 返回 HTTP 403 Forbidden
        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // 2. 普通用户访问模块趋势接口 -> 返回 HTTP 403 Forbidden
        mockMvc.perform(get("/api/admin/dashboard/module-trends")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // 3. 普通用户访问操作审计日志接口 -> 返回 HTTP 403 Forbidden
        mockMvc.perform(get("/api/admin/operations/usage-logs")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // 4. 普通用户访问模块使用排行接口 -> 返回 HTTP 403 Forbidden
        mockMvc.perform(get("/api/admin/operations/module-ranking")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("测试管理端看板概览：用户指标、模块排行、健康分布、收支汇总与净结余核对")
    void testAdminDashboardOverviewAndMetrics() throws Exception {
        String adminToken = createAdminUserAndGetToken("board_admin", "Admin123456");
        String user1Token = registerAndGetToken("stat_user_one", "User123456");
        String user2Token = registerAndGetToken("stat_user_two", "User123456");

        SysUser user1 = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "stat_user_one"));
        SysUser user2 = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "stat_user_two"));

        // 1. 模拟写入 module_usage_log
        moduleUsageLogMapper.insert(new ModuleUsageLog(user1.getId(), "TODO", "CREATE", 101L, "req-1"));
        moduleUsageLogMapper.insert(new ModuleUsageLog(user1.getId(), "TODO", "COMPLETE", 101L, "req-2"));
        moduleUsageLogMapper.insert(new ModuleUsageLog(user2.getId(), "TODO", "CREATE", 102L, "req-3"));
        moduleUsageLogMapper.insert(new ModuleUsageLog(user1.getId(), "POMODORO", "FINISH", 201L, "req-4"));
        moduleUsageLogMapper.insert(new ModuleUsageLog(user2.getId(), "HABIT", "CHECKIN", 301L, "req-5"));
        moduleUsageLogMapper.insert(new ModuleUsageLog(user1.getId(), "FINANCE", "CREATE", 401L, "req-6"));

        // 2. 模拟写入健康数据
        HealthRecord hrSteps = new HealthRecord();
        hrSteps.setUserId(user1.getId());
        hrSteps.setMetricCode("STEPS");
        hrSteps.setMetricValue(new BigDecimal("8500.00"));
        hrSteps.setUnit("步");
        hrSteps.setRecordTime(LocalDateTime.now());
        healthRecordMapper.insert(hrSteps);

        HealthRecord hrSleep = new HealthRecord();
        hrSleep.setUserId(user2.getId());
        hrSleep.setMetricCode("SLEEP_HOURS");
        hrSleep.setMetricValue(new BigDecimal("7.50"));
        hrSleep.setUnit("小时");
        hrSleep.setRecordTime(LocalDateTime.now());
        healthRecordMapper.insert(hrSleep);

        // 3. 模拟写入财务数据（收入 5000，支出 1200，净结余 = 3800）
        FinanceRecord income = new FinanceRecord(user1.getId(), null, "INCOME", new BigDecimal("5000.00"), "CNY", LocalDate.now(), "发工资", "奖金");
        financeRecordMapper.insert(income);

        FinanceRecord expense = new FinanceRecord(user1.getId(), null, "EXPENSE", new BigDecimal("1200.00"), "CNY", LocalDate.now(), "买鞋", "消费");
        financeRecordMapper.insert(expense);

        // 4. 模拟写入番茄专注数据（实际时长 1500 秒 = 25 分钟）
        PomodoroRecord pomodoro = new PomodoroRecord();
        pomodoro.setUserId(user1.getId());
        pomodoro.setSessionType("FOCUS");
        pomodoro.setPlannedSeconds(1500);
        pomodoro.setActualSeconds(1500);
        pomodoro.setStatus("COMPLETED");
        pomodoro.setStartedAt(LocalDateTime.now().minusMinutes(25));
        pomodoro.setEndedAt(LocalDateTime.now());
        pomodoro.setCreatedAt(LocalDateTime.now().minusMinutes(25));
        pomodoro.setUpdatedAt(LocalDateTime.now());
        pomodoroRecordMapper.insert(pomodoro);

        // 5. 模拟写入待办任务
        TodoTask todo1 = new TodoTask();
        todo1.setUserId(user1.getId());
        todo1.setTitle("任务1");
        todo1.setStatus("DONE");
        todoTaskMapper.insert(todo1);

        TodoTask todo2 = new TodoTask();
        todo2.setUserId(user2.getId());
        todo2.setTitle("任务2");
        todo2.setStatus("TODO");
        todoTaskMapper.insert(todo2);

        // 6. 模拟写入习惯打卡
        HabitCheckin checkin = new HabitCheckin(user2.getId(), 1L, LocalDate.now(), "已完成早读", 0);
        checkin.setCreatedAt(LocalDateTime.now());
        habitCheckinMapper.insert(checkin);

        // 7. 发起管理端看板请求 /api/admin/dashboard/overview
        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.overviewCards.totalUserCount", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data.overviewCards.rangeActiveUserCount", equalTo(2)))
                .andExpect(jsonPath("$.data.overviewCards.todayActiveUserCount", equalTo(2)))
                .andExpect(jsonPath("$.data.overviewCards.totalHealthRecordCount", equalTo(2)))
                .andExpect(jsonPath("$.data.overviewCards.rangeHealthRecordCount", equalTo(2)))
                .andExpect(jsonPath("$.data.overviewCards.totalExpenseAmount", equalTo(1200.00)))
                .andExpect(jsonPath("$.data.overviewCards.rangeExpenseAmount", equalTo(1200.00)))
                .andExpect(jsonPath("$.data.overviewCards.totalIncomeAmount", equalTo(5000.00)))
                .andExpect(jsonPath("$.data.overviewCards.rangeIncomeAmount", equalTo(5000.00)))
                .andExpect(jsonPath("$.data.overviewCards.rangeNetBalance", equalTo(3800.00)))
                .andExpect(jsonPath("$.data.overviewCards.rangeFocusMinutes", equalTo(25)))
                .andExpect(jsonPath("$.data.overviewCards.totalTodoCount", equalTo(2)))
                .andExpect(jsonPath("$.data.overviewCards.completedTodoCount", equalTo(1)))
                .andExpect(jsonPath("$.data.overviewCards.totalHabitCheckinCount", equalTo(1)))
                // 模块排行验证：TODO 共有 3 条，占总数 6 条的 50%
                .andExpect(jsonPath("$.data.moduleUsageRank[0].moduleCode", equalTo("TODO")))
                .andExpect(jsonPath("$.data.moduleUsageRank[0].count", equalTo(3)))
                .andExpect(jsonPath("$.data.moduleUsageRank[0].percentage", equalTo(50.0)))
                // 财务统计验证
                .andExpect(jsonPath("$.data.financeSummary.totalIncome", equalTo(5000.00)))
                .andExpect(jsonPath("$.data.financeSummary.totalExpense", equalTo(1200.00)))
                .andExpect(jsonPath("$.data.financeSummary.netBalance", equalTo(3800.00)))
                .andExpect(jsonPath("$.data.financeSummary.recordCount", equalTo(2)))
                // 用户趋势列表验证
                .andExpect(jsonPath("$.data.userTrend", hasSize(greaterThanOrEqualTo(7))));
    }

    @Test
    @DisplayName("测试业务模块每日调用趋势与堆叠数据接口")
    void testAdminModuleTrends() throws Exception {
        String adminToken = createAdminUserAndGetToken("trend_admin", "Admin123456");
        String userToken = registerAndGetToken("trend_user", "User123456");

        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "trend_user"));
        moduleUsageLogMapper.insert(new ModuleUsageLog(user.getId(), "TODO", "CREATE", 501L, "req-t1"));
        moduleUsageLogMapper.insert(new ModuleUsageLog(user.getId(), "HEALTH", "CREATE", 502L, "req-t2"));

        mockMvc.perform(get("/api/admin/dashboard/module-trends")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.dates", hasSize(greaterThanOrEqualTo(7))))
                .andExpect(jsonPath("$.data.series", hasSize(6))) // TODO, POMODORO, HABIT, HEALTH, FINANCE, GOAL
                .andExpect(jsonPath("$.data.series[0].moduleCode", equalTo("TODO")));
    }

    @Test
    @DisplayName("测试模块使用审计日志分页查询与排行接口")
    void testAdminOperationUsageLogsPaginationAndFilter() throws Exception {
        String adminToken = createAdminUserAndGetToken("oper_admin", "Admin123456");
        String userToken = registerAndGetToken("oper_user", "User123456");

        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "oper_user"));

        // 写入 5 条日志
        for (int i = 1; i <= 5; i++) {
            moduleUsageLogMapper.insert(new ModuleUsageLog(user.getId(), "TODO", "CREATE", 600L + i, "req-oper-" + i));
        }

        // 1. 分页查询，pageSize = 3
        mockMvc.perform(get("/api/admin/operations/usage-logs")
                        .param("moduleCode", "TODO")
                        .param("current", "1")
                        .param("size", "3")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total", equalTo(5)))
                .andExpect(jsonPath("$.data.pageNum", equalTo(1)))
                .andExpect(jsonPath("$.data.pageSize", equalTo(3)))
                .andExpect(jsonPath("$.data.records", hasSize(3)))
                .andExpect(jsonPath("$.data.records[0].username", equalTo("oper_user")))
                .andExpect(jsonPath("$.data.records[0].moduleName", equalTo("待办清单")))
                .andExpect(jsonPath("$.data.records[0].actionName", equalTo("新增/创建")));

        // 2. 模块使用排行接口
        mockMvc.perform(get("/api/admin/operations/module-ranking")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].moduleCode", equalTo("TODO")))
                .andExpect(jsonPath("$.data[0].count", equalTo(5)));
    }

    @Test
    @DisplayName("测试时间范围校验：开始日期晚于结束日期或超过90天时拒绝请求")
    void testAdminDashboardDateRangeValidation() throws Exception {
        String adminToken = createAdminUserAndGetToken("valid_admin", "Admin123456");

        // 1. 开始日期晚于结束日期
        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .param("startDate", "2026-11-10")
                        .param("endDate", "2026-11-01")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("开始日期不能晚于结束日期")));

        // 2. 时间跨度超过 90 天
        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-06-01")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("不能超过90天")));
    }
}
