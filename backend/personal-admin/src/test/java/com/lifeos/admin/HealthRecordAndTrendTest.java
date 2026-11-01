package com.lifeos.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.business.dto.CreateHealthRecordRequest;
import com.lifeos.business.dto.HealthStandardDTO;
import com.lifeos.business.dto.UpdateHealthRecordRequest;
import com.lifeos.business.entity.HealthRecord;
import com.lifeos.business.entity.ModuleUsageLog;
import com.lifeos.business.mapper.HealthRecordMapper;
import com.lifeos.business.mapper.HealthStandardMapper;
import com.lifeos.business.mapper.ModuleUsageLogMapper;
import com.lifeos.system.dto.LoginDTO;
import com.lifeos.system.dto.RegisterDTO;
import com.lifeos.system.entity.SysRole;
import com.lifeos.system.entity.SysUser;
import com.lifeos.system.entity.SysUserRole;
import com.lifeos.system.mapper.SysRoleMapper;
import com.lifeos.system.mapper.SysUserMapper;
import com.lifeos.system.mapper.SysUserRoleMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("健康指标录入、多维趋势聚合与参考标准集成测试")
class HealthRecordAndTrendTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Autowired
    private HealthStandardMapper healthStandardMapper;

    @Autowired
    private ModuleUsageLogMapper moduleUsageLogMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String registerAndGetToken(String username, String password) throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(username);
        registerDTO.setPassword(password);
        registerDTO.setPhone("139" + System.currentTimeMillis() % 100000000);
        registerDTO.setNickname("用户" + username);

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
        admin.setNickname("健康管理员");
        admin.setStatus(SysUser.STATUS_ENABLED);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        admin.setDeleted(0);
        sysUserMapper.insert(admin);

        SysRole adminRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, SysRole.ROLE_ADMIN));
        sysUserRoleMapper.insert(new SysUserRole(admin.getId(), adminRole.getId()));

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDTO(username, password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    @Test
    @DisplayName("测试正常录入多类型健康数据与默认单位自动填充")
    void testCreateHealthRecordSuccess() throws Exception {
        String token = registerAndGetToken("health_user_01", "Pass123456");

        // 1. 录入步数（未传单位，自动填充「步」）
        CreateHealthRecordRequest stepsReq = new CreateHealthRecordRequest();
        stepsReq.setMetricCode("STEPS");
        stepsReq.setMetricValue(BigDecimal.valueOf(8500));
        stepsReq.setNote("晨跑加日常散步");

        mockMvc.perform(post("/api/health/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stepsReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.metricCode").value("STEPS"))
                .andExpect(jsonPath("$.data.unit").value("步"))
                .andExpect(jsonPath("$.data.metricValue").value(8500))
                .andExpect(jsonPath("$.data.standardStatus").value("NORMAL"))
                .andExpect(jsonPath("$.data.standardHint", containsString("非医疗诊断")));

        // 2. 录入睡眠（小时）
        CreateHealthRecordRequest sleepReq = new CreateHealthRecordRequest();
        sleepReq.setMetricCode("SLEEP_HOURS");
        sleepReq.setMetricValue(BigDecimal.valueOf(7.5));
        sleepReq.setUnit("h");

        mockMvc.perform(post("/api/health/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sleepReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.unit").value("h"))
                .andExpect(jsonPath("$.data.metricValue").value(7.5));

        // 3. 验证模块操作日志记录
        Long count = moduleUsageLogMapper.selectCount(new LambdaQueryWrapper<ModuleUsageLog>()
                .eq(ModuleUsageLog::getModuleCode, "HEALTH")
                .eq(ModuleUsageLog::getActionCode, "CREATE_RECORD"));
        assertTrue(count >= 2);
    }

    @Test
    @DisplayName("测试健康数据合法性校验（非法指标类型、超界数值、不支持单位）")
    void testMetricValidationErrors() throws Exception {
        String token = registerAndGetToken("health_user_02", "Pass123456");

        // 1. 非法指标类型
        CreateHealthRecordRequest req1 = new CreateHealthRecordRequest();
        req1.setMetricCode("UNKNOWN_METRIC");
        req1.setMetricValue(BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/health/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("不支持的健康指标类型")));

        // 2. 步数超出合理极值 (如 300,000 步)
        CreateHealthRecordRequest req2 = new CreateHealthRecordRequest();
        req2.setMetricCode("STEPS");
        req2.setMetricValue(BigDecimal.valueOf(300000));

        mockMvc.perform(post("/api/health/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("超出合理范围")));

        // 3. 睡眠时长超过 24 小时
        CreateHealthRecordRequest req3 = new CreateHealthRecordRequest();
        req3.setMetricCode("SLEEP_HOURS");
        req3.setMetricValue(BigDecimal.valueOf(25.0));

        mockMvc.perform(post("/api/health/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        // 4. 不支持的单位 (例如步数使用 km)
        CreateHealthRecordRequest req4 = new CreateHealthRecordRequest();
        req4.setMetricCode("STEPS");
        req4.setMetricValue(BigDecimal.valueOf(5000));
        req4.setUnit("km");

        mockMvc.perform(post("/api/health/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("指标单位 [km] 不合法")));
    }

    @Test
    @DisplayName("测试修改与删除健康记录")
    void testHealthRecordUpdateAndDelete() throws Exception {
        String token = registerAndGetToken("health_user_03", "Pass123456");

        // 创建一条体重记录
        CreateHealthRecordRequest req = new CreateHealthRecordRequest();
        req.setMetricCode("WEIGHT");
        req.setMetricValue(BigDecimal.valueOf(68.5));
        req.setUnit("kg");

        MvcResult createResult = mockMvc.perform(post("/api/health/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        long recordId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 修改体重数值
        UpdateHealthRecordRequest updateReq = new UpdateHealthRecordRequest();
        updateReq.setMetricValue(BigDecimal.valueOf(67.8));
        updateReq.setNote("早起空腹称重");

        mockMvc.perform(put("/api/health/records/" + recordId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.metricValue").value(67.8))
                .andExpect(jsonPath("$.data.note").value("早起空腹称重"));

        // 删除该记录
        mockMvc.perform(delete("/api/health/records/" + recordId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 再次查询已删除的记录应返回 404
        mockMvc.perform(get("/api/health/records/" + recordId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("测试跨用户健康数据严格隔离")
    void testHealthRecordDataIsolation() throws Exception {
        String tokenUserA = registerAndGetToken("health_alice", "Pass123456");
        String tokenUserB = registerAndGetToken("health_bob", "Pass123456");

        // Alice 录入步数 10000
        CreateHealthRecordRequest aliceReq = new CreateHealthRecordRequest();
        aliceReq.setMetricCode("STEPS");
        aliceReq.setMetricValue(BigDecimal.valueOf(10000));

        MvcResult res = mockMvc.perform(post("/api/health/records")
                        .header("Authorization", "Bearer " + tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aliceReq)))
                .andExpect(status().isOk())
                .andReturn();

        long aliceRecordId = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // Bob 查询列表，应为空，无法看到 Alice 的记录
        mockMvc.perform(get("/api/health/records")
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records", hasSize(0)))
                .andExpect(jsonPath("$.data.total").value(0));

        // Bob 尝试修改 Alice 的记录，应返回 404
        UpdateHealthRecordRequest bobUpdate = new UpdateHealthRecordRequest();
        bobUpdate.setMetricValue(BigDecimal.valueOf(12000));

        mockMvc.perform(put("/api/health/records/" + aliceRecordId)
                        .header("Authorization", "Bearer " + tokenUserB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bobUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // Bob 尝试删除 Alice 的记录，应返回 404
        mockMvc.perform(delete("/api/health/records/" + aliceRecordId)
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("测试健康趋势数据聚合与非医疗免责声明")
    void testHealthTrendsAggregation() throws Exception {
        String token = registerAndGetToken("health_trend_user", "Pass123456");

        // 录入今天的两次步数打卡（2000 + 4500 = 6500）
        CreateHealthRecordRequest r1 = new CreateHealthRecordRequest();
        r1.setMetricCode("STEPS");
        r1.setMetricValue(BigDecimal.valueOf(2000));
        r1.setRecordTime(LocalDateTime.now().minusHours(4));

        CreateHealthRecordRequest r2 = new CreateHealthRecordRequest();
        r2.setMetricCode("STEPS");
        r2.setMetricValue(BigDecimal.valueOf(4500));
        r2.setRecordTime(LocalDateTime.now());

        mockMvc.perform(post("/api/health/records")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(r1))).andExpect(status().isOk());

        mockMvc.perform(post("/api/health/records")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(r2))).andExpect(status().isOk());

        // 查询最近 7 天步数趋势
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);

        mockMvc.perform(get("/api/health/trends")
                        .header("Authorization", "Bearer " + token)
                        .param("metricCode", "STEPS")
                        .param("startDate", startDate.toString())
                        .param("endDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.metricCode").value("STEPS"))
                .andExpect(jsonPath("$.data.aggregateMethod").value("SUM"))
                .andExpect(jsonPath("$.data.points", hasSize(7)))
                // 今日累计值为 6500，打卡次数 2
                .andExpect(jsonPath("$.data.points[6].value").value(6500))
                .andExpect(jsonPath("$.data.points[6].recordCount").value(2))
                .andExpect(jsonPath("$.data.disclaimer", containsString("不作为任何医学诊断凭据")));
    }

    @Test
    @DisplayName("测试今日健康看板快照汇总与达标评估")
    void testHealthDashboardSummary() throws Exception {
        String token = registerAndGetToken("health_summary_user", "Pass123456");

        // 录入今日步数 8000（在参考区间 6000-12000 内）
        CreateHealthRecordRequest stepsReq = new CreateHealthRecordRequest();
        stepsReq.setMetricCode("STEPS");
        stepsReq.setMetricValue(BigDecimal.valueOf(8000));
        mockMvc.perform(post("/api/health/records")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stepsReq))).andExpect(status().isOk());

        // 录入饮水量 1800ml（在参考区间 1500-2500 内）
        CreateHealthRecordRequest waterReq = new CreateHealthRecordRequest();
        waterReq.setMetricCode("WATER_ML");
        waterReq.setMetricValue(BigDecimal.valueOf(1800));
        mockMvc.perform(post("/api/health/records")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(waterReq))).andExpect(status().isOk());

        // 录入最新体重 65.0kg
        CreateHealthRecordRequest weightReq = new CreateHealthRecordRequest();
        weightReq.setMetricCode("WEIGHT");
        weightReq.setMetricValue(BigDecimal.valueOf(65.0));
        mockMvc.perform(post("/api/health/records")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(weightReq))).andExpect(status().isOk());

        // 获取看板快照
        mockMvc.perform(get("/api/health/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todaySteps").value(8000))
                .andExpect(jsonPath("$.data.stepsStatus").value("NORMAL"))
                .andExpect(jsonPath("$.data.todayWaterMl").value(1800))
                .andExpect(jsonPath("$.data.waterStatus").value("NORMAL"))
                .andExpect(jsonPath("$.data.latestWeight").value(65.0))
                .andExpect(jsonPath("$.data.weightStatus").value("NORMAL"))
                .andExpect(jsonPath("$.data.sleepStatus").value("NOT_RECORDED"))
                .andExpect(jsonPath("$.data.disclaimer", containsString("绝不构成任何医学诊断")));
    }

    @Test
    @DisplayName("测试参考标准公开只读与管理端权限受控")
    void testHealthStandardsAndAdminConfiguration() throws Exception {
        String userToken = registerAndGetToken("normal_health_user", "Pass123456");
        String adminToken = createAdminUserAndGetToken("health_admin", "AdminPass123");

        // 1. 普通用户可获取当前生效的标准列表
        mockMvc.perform(get("/api/health/standards")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", not(empty())));

        // 2. 普通用户尝试访问管理端配置标准，预期被拦截 (403 Forbidden)
        HealthStandardDTO newStandard = new HealthStandardDTO();
        newStandard.setMetricCode("STEPS");
        newStandard.setMetricName("每日极限步数标准");
        newStandard.setUnit("步");
        newStandard.setMinValue(BigDecimal.valueOf(8000));
        newStandard.setMaxValue(BigDecimal.valueOf(15000));

        mockMvc.perform(post("/api/admin/health-standards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStandard)))
                .andExpect(status().isForbidden());

        // 3. 管理员用户创建新标准，成功返回
        MvcResult adminCreateRes = mockMvc.perform(post("/api/admin/health-standards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStandard)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.metricName").value("每日极限步数标准"))
                .andReturn();

        long stdId = objectMapper.readTree(adminCreateRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 4. 管理员修改该标准
        newStandard.setMetricName("每日进阶步数标准");
        mockMvc.perform(put("/api/admin/health-standards/" + stdId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStandard)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.metricName").value("每日进阶步数标准"));

        // 5. 管理员删除该标准
        mockMvc.perform(delete("/api/admin/health-standards/" + stdId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}

