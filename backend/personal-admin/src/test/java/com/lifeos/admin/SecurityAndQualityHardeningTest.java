package com.lifeos.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.business.dto.*;
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
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 阶段 14：系统安全防御、权限边界与数据隔离综合加固测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("阶段 14：质量与安全加固综合测试")
class SecurityAndQualityHardeningTest {

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

    private String registerAndGetToken(String username, String password) throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDTO(username, password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    private String createAdminUserAndGetToken(String username, String password) throws Exception {
        SysUser admin = new SysUser();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setNickname("加固管理员");
        admin.setStatus(SysUser.STATUS_ENABLED);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        admin.setDeleted(0);
        sysUserMapper.insert(admin);

        SysRole adminRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, SysRole.ROLE_ADMIN));
        if (adminRole == null) {
            adminRole = new SysRole();
            adminRole.setRoleCode(SysRole.ROLE_ADMIN);
            adminRole.setRoleName("管理员");
            adminRole.setCreatedAt(LocalDateTime.now());
            sysRoleMapper.insert(adminRole);
        }
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
    @DisplayName("安全加固 1: 垂直越权防御 - 普通用户与未认证请求严禁访问任何管理端接口")
    void testVerticalPrivilegeEscalationDefense() throws Exception {
        String userToken = registerAndGetToken("normal_sec_user", "sec123456");
        String adminToken = createAdminUserAndGetToken("admin_sec_user", "admin123456");

        // 1. 未登录访问管理端看板 -> 401 Unauthorized
        mockMvc.perform(get("/api/admin/dashboard/overview"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(401)));

        // 2. 普通用户访问管理端看板概览 -> 403 Forbidden
        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(403)));

        // 3. 普通用户访问管理端模块趋势 -> 403 Forbidden
        mockMvc.perform(get("/api/admin/dashboard/module-trends")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(403)));

        // 4. 普通用户访问管理端操作日志 -> 403 Forbidden
        mockMvc.perform(get("/api/admin/operations/usage-logs")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(403)));

        // 5. 普通用户访问管理端模块排行 -> 403 Forbidden
        mockMvc.perform(get("/api/admin/operations/module-ranking")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(403)));

        // 6. 普通用户尝试新增管理端收支分类 -> 403 Forbidden
        mockMvc.perform(post("/api/admin/finance-categories")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"恶意分类\",\"type\":\"EXPENSE\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(403)));

        // 7. 管理员正常访问管理端接口 -> 200 OK
        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.overviewCards", notNullValue()));
    }

    @Test
    @DisplayName("安全加固 2: 水平越权防御 - 用户 B 绝对无法获取、修改或删除用户 A 的全业务资源")
    void testHorizontalPrivilegeEscalationDefense() throws Exception {
        String tokenAlice = registerAndGetToken("alice_hardening", "pass123456");
        String tokenBob = registerAndGetToken("bob_hardening", "pass123456");

        // 1. Alice 创建待办
        CreateTodoRequest createTodo = new CreateTodoRequest();
        createTodo.setTitle("Alice 绝密任务");
        MvcResult todoRes = mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTodo)))
                .andExpect(status().isOk())
                .andReturn();
        Long aliceTodoId = objectMapper.readTree(todoRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 2. Alice 创建目标
        CreateGoalRequest createGoal = new CreateGoalRequest();
        createGoal.setTitle("Alice 年度愿景");
        createGoal.setStartDate(LocalDate.now());
        createGoal.setDueDate(LocalDate.now().plusMonths(3));
        MvcResult goalRes = mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGoal)))
                .andExpect(status().isOk())
                .andReturn();
        Long aliceGoalId = objectMapper.readTree(goalRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 3. Alice 创建健康记录
        CreateHealthRecordRequest createHealth = new CreateHealthRecordRequest();
        createHealth.setMetricCode("WEIGHT");
        createHealth.setMetricValue(new BigDecimal("55.5"));
        createHealth.setRecordTime(LocalDateTime.now());
        MvcResult healthRes = mockMvc.perform(post("/api/health/records")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHealth)))
                .andExpect(status().isOk())
                .andReturn();
        Long aliceHealthId = objectMapper.readTree(healthRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 4. Alice 创建习惯
        CreateHabitRequest createHabit = new CreateHabitRequest();
        createHabit.setName("早起晨读");
        createHabit.setFrequency("DAILY");
        createHabit.setStartDate(LocalDate.now());
        MvcResult habitRes = mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHabit)))
                .andExpect(status().isOk())
                .andReturn();
        Long aliceHabitId = objectMapper.readTree(habitRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 5. Alice 创建日记
        CreateDiaryRequest createDiary = new CreateDiaryRequest();
        createDiary.setTitle("Alice 私人日记");
        createDiary.setContent("这是非常私密的日记内容，他人绝不可见");
        createDiary.setMood("HAPPY");
        createDiary.setDiaryDate(LocalDate.now());
        MvcResult diaryRes = mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDiary)))
                .andExpect(status().isOk())
                .andReturn();
        Long aliceDiaryId = objectMapper.readTree(diaryRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 6. Alice 创建备忘录
        CreateMemoRequest createMemo = new CreateMemoRequest();
        createMemo.setTitle("Alice 银行卡备忘");
        createMemo.setContent("卡号 6222000000000000");
        MvcResult memoRes = mockMvc.perform(post("/api/memos")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMemo)))
                .andExpect(status().isOk())
                .andReturn();
        Long aliceMemoId = objectMapper.readTree(memoRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 7. Alice 创建物品
        CreateItemRequest createItem = new CreateItemRequest();
        createItem.setItemName("Kindle 电子书阅读器");
        MvcResult itemRes = mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createItem)))
                .andExpect(status().isOk())
                .andReturn();
        Long aliceItemId = objectMapper.readTree(itemRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 8. Alice 创建提醒规则
        CreateReminderRuleRequest createRule = new CreateReminderRuleRequest();
        createRule.setRuleName("定时待办提醒");
        createRule.setTriggerType("TODO_DUE");
        createRule.setBusinessRefId(aliceTodoId);
        createRule.setRemindAt(LocalTime.of(10, 0));
        createRule.setRepeatRule("DAILY");
        MvcResult ruleRes = mockMvc.perform(post("/api/reminders/rules")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRule)))
                .andExpect(status().isOk())
                .andReturn();
        Long aliceRuleId = objectMapper.readTree(ruleRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // ==========================
        // 开始 Bob 的水平越权攻击尝试
        // ==========================

        // Bob 尝试查询 Alice 的待办 -> 404
        mockMvc.perform(get("/api/todos/" + aliceTodoId)
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(404)));

        // Bob 尝试删除 Alice 的待办 -> 404
        mockMvc.perform(delete("/api/todos/" + aliceTodoId)
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(404)));

        // Bob 尝试查询 Alice 的目标 -> 404
        mockMvc.perform(get("/api/goals/" + aliceGoalId)
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(404)));

        // Bob 尝试向 Alice 的目标注入子任务 -> 404
        CreateGoalTaskRequest bobSubtask = new CreateGoalTaskRequest();
        bobSubtask.setTitle("恶弟子任务");
        mockMvc.perform(post("/api/goals/" + aliceGoalId + "/tasks")
                        .header("Authorization", "Bearer " + tokenBob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bobSubtask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(404)));

        // Bob 尝试查看 Alice 的健康记录 -> 404
        mockMvc.perform(get("/api/health/records/" + aliceHealthId)
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(404)));

        // Bob 尝试删除 Alice 的健康记录 -> 404
        mockMvc.perform(delete("/api/health/records/" + aliceHealthId)
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(404)));

        // Bob 尝试给 Alice 的习惯打卡 -> 404
        mockMvc.perform(post("/api/habits/" + aliceHabitId + "/checkins")
                        .header("Authorization", "Bearer " + tokenBob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"checkinDate\":\"" + LocalDate.now() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(404)));

        // Bob 尝试窥探 Alice 的日记正文 -> 404
        mockMvc.perform(get("/api/diaries/" + aliceDiaryId)
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(404)));

        // Bob 尝试删除 Alice 的日记 -> 404
        mockMvc.perform(delete("/api/diaries/" + aliceDiaryId)
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(404)));

        // Bob 尝试查看 Alice 的备忘录 -> 404
        mockMvc.perform(get("/api/memos/" + aliceMemoId)
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(404)));

        // Bob 尝试出借 Alice 的物品 -> 404
        CreateLendingRequest bobBorrow = new CreateLendingRequest();
        bobBorrow.setBorrowerName("入侵者");
        bobBorrow.setLentAt(LocalDateTime.now());
        mockMvc.perform(post("/api/items/" + aliceItemId + "/lendings")
                        .header("Authorization", "Bearer " + tokenBob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bobBorrow)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(404)));

        // Bob 尝试切换 Alice 的提醒规则状态 -> 404
        mockMvc.perform(put("/api/reminders/rules/" + aliceRuleId + "/status")
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(404)));
    }

    @Test
    @DisplayName("安全加固 3: 分页超限防御与 SQL 模糊搜索注入防御")
    void testPaginationAndSqlInjectionDefense() throws Exception {
        String token = registerAndGetToken("query_sec_user", "pass123456");

        // 1. 尝试使用极端 pageSize=99999999 进行待办查询
        MvcResult res = mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .param("pageSize", "99999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(res.getResponse().getContentAsString());
        int pageSize = jsonNode.path("data").path("pageSize").asInt();
        assertTrue(pageSize <= 100, "待办分页必须被强制限制在 100 以内，实际为: " + pageSize);

        // 2. 尝试使用极端 size=88888 进行提醒消息查询
        MvcResult resMsg = mockMvc.perform(get("/api/reminders/messages")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "88888"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andReturn();

        int msgPageSize = objectMapper.readTree(resMsg.getResponse().getContentAsString())
                .path("data").path("pageSize").asInt();
        assertTrue(msgPageSize <= 100, "消息分页必须被强制限制在 100 以内，实际为: " + msgPageSize);

        // 3. 尝试 SQL 注入语法作为搜索关键字
        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .param("keyword", "' OR 1=1 -- "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.records", hasSize(0)));

        mockMvc.perform(get("/api/diaries")
                        .header("Authorization", "Bearer " + token)
                        .param("keyword", "'; DROP TABLE diary; --"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.records", hasSize(0)));
    }

    @Test
    @DisplayName("安全加固 4: 业务数据合法性与防重防御 - 日记同天唯一性与时间倒错拦截")
    void testBusinessValidationAndDuplicateDefense() throws Exception {
        String token = registerAndGetToken("valid_sec_user", "pass123456");

        // 1. 待办时间倒错拦截 (startAt 晚于 dueAt)
        CreateTodoRequest invalidTimeTodo = new CreateTodoRequest();
        invalidTimeTodo.setTitle("倒错任务");
        invalidTimeTodo.setStartAt(LocalDateTime.now().plusDays(5));
        invalidTimeTodo.setDueAt(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTimeTodo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", containsString("计划开始时间不得晚于截止时间")));

        // 2. 同一天重复创建日记防御
        CreateDiaryRequest diary1 = new CreateDiaryRequest();
        diary1.setTitle("第一次记录");
        diary1.setContent("日记正文 1");
        diary1.setDiaryDate(LocalDate.now());

        mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diary1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)));

        // 再次创建同天日记，预期被拦截
        CreateDiaryRequest diary2 = new CreateDiaryRequest();
        diary2.setTitle("重复创建");
        diary2.setContent("日记正文 2");
        diary2.setDiaryDate(LocalDate.now());

        mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diary2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", containsString("已经记录过日记")));
    }
}

