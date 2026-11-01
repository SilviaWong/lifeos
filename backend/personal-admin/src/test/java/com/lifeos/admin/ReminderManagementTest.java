package com.lifeos.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.business.dto.*;
import com.lifeos.business.entity.*;
import com.lifeos.business.mapper.*;
import com.lifeos.business.service.ReminderSchedulerService;
import com.lifeos.system.dto.LoginDTO;
import com.lifeos.system.dto.RegisterDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("提醒规则与站内消息：规则管理、定时调度、严格幂等性与跨用户数据隔离集成测试")
class ReminderManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReminderRuleMapper reminderRuleMapper;

    @Autowired
    private ReminderMessageMapper reminderMessageMapper;

    @Autowired
    private ReminderExecutionLogMapper reminderExecutionLogMapper;

    @Autowired
    private TodoTaskMapper todoTaskMapper;

    @Autowired
    private HabitMapper habitMapper;

    @Autowired
    private HabitCheckinMapper habitCheckinMapper;

    @Autowired
    private GoalMapper goalMapper;

    @Autowired
    private ReminderSchedulerService reminderSchedulerService;

    private String registerAndGetToken(String username, String password) throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(username);
        registerDTO.setPassword(password);
        registerDTO.setPhone("137" + System.currentTimeMillis() % 100000000);
        registerDTO.setNickname("TestUser_" + username);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk());

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(username);
        loginDTO.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).path("data").path("token").asText();
    }

    @Test
    @DisplayName("测试提醒规则管理：创建、查询详情、修改、停用启用切换、列表与删除")
    void testReminderRuleCrudAndToggleStatus() throws Exception {
        String token = registerAndGetToken("rule_user_1", "Pass123456");

        // 1. 创建规则
        CreateReminderRuleRequest req = new CreateReminderRuleRequest();
        req.setRuleName("待办任务临期提醒");
        req.setTriggerType("TODO_DUE");
        req.setAdvanceMinutes(15);
        req.setRepeatRule("DAILY");
        req.setStatus(1);

        MvcResult createRes = mockMvc.perform(post("/api/reminders/rules")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.ruleName").value("待办任务临期提醒"))
                .andExpect(jsonPath("$.data.status").value(1))
                .andReturn();

        Long ruleId = objectMapper.readTree(createRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. 详情查询
        mockMvc.perform(get("/api/reminders/rules/" + ruleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(ruleId))
                .andExpect(jsonPath("$.data.triggerType").value("TODO_DUE"))
                .andExpect(jsonPath("$.data.advanceMinutes").value(15));

        // 3. 修改规则
        UpdateReminderRuleRequest updateReq = new UpdateReminderRuleRequest();
        updateReq.setRuleName("工作日待办提前30分钟提醒");
        updateReq.setTriggerType("TODO_DUE");
        updateReq.setAdvanceMinutes(30);
        updateReq.setRepeatRule("WORKDAY");
        updateReq.setStatus(1);

        mockMvc.perform(put("/api/reminders/rules/" + ruleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ruleName").value("工作日待办提前30分钟提醒"))
                .andExpect(jsonPath("$.data.advanceMinutes").value(30))
                .andExpect(jsonPath("$.data.repeatRule").value("WORKDAY"));

        // 4. 停用规则
        mockMvc.perform(put("/api/reminders/rules/" + ruleId + "/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(0));

        // 5. 再次启用
        mockMvc.perform(put("/api/reminders/rules/" + ruleId + "/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1));

        // 6. 分页查询
        mockMvc.perform(get("/api/reminders/rules")
                        .param("triggerType", "TODO_DUE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(ruleId));

        // 7. 删除规则
        mockMvc.perform(delete("/api/reminders/rules/" + ruleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 确认删除后 404
        mockMvc.perform(get("/api/reminders/rules/" + ruleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("测试站内消息生命周期：未读统计、按状态查询、单条标已读、一键全部标已读与删除")
    void testReminderMessageLifecycleAndReadStates() throws Exception {
        String token = registerAndGetToken("msg_user_1", "Pass123456");

        // 直接插入 3 条测试站内消息：2 条未读，1 条已读
        ReminderMessage msg1 = new ReminderMessage();
        msg1.setUserId(1L);
        msg1.setTitle("测试待办提醒");
        msg1.setContent("您有一个待办任务即将到期");
        msg1.setMessageType("TODO");
        msg1.setStatus("SENT");
        msg1.setScheduledAt(LocalDateTime.now());
        msg1.setSentAt(LocalDateTime.now());
        msg1.setCreatedAt(LocalDateTime.now());
        msg1.setDeleted(0);
        reminderMessageMapper.insert(msg1);

        ReminderMessage msg2 = new ReminderMessage();
        msg2.setUserId(1L);
        msg2.setTitle("测试习惯提醒");
        msg2.setContent("您今天还没完成早起打卡");
        msg2.setMessageType("HABIT");
        msg2.setStatus("SENT");
        msg2.setScheduledAt(LocalDateTime.now());
        msg2.setSentAt(LocalDateTime.now());
        msg2.setCreatedAt(LocalDateTime.now());
        msg2.setDeleted(0);
        reminderMessageMapper.insert(msg2);

        ReminderMessage msg3 = new ReminderMessage();
        msg3.setUserId(1L);
        msg3.setTitle("测试系统公告");
        msg3.setContent("系统维护通知");
        msg3.setMessageType("SYSTEM");
        msg3.setStatus("READ");
        msg3.setScheduledAt(LocalDateTime.now().minusDays(1));
        msg3.setSentAt(LocalDateTime.now().minusDays(1));
        msg3.setReadAt(LocalDateTime.now().minusDays(1));
        msg3.setCreatedAt(LocalDateTime.now().minusDays(1));
        msg3.setDeleted(0);
        reminderMessageMapper.insert(msg3);

        // 1. 查询未读数，预期为 2
        mockMvc.perform(get("/api/reminders/messages/unread-count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(2));

        // 2. 查询汇总概览
        mockMvc.perform(get("/api/reminders/messages/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadMessagesCount").value(2));

        // 3. 筛选未读消息列表
        mockMvc.perform(get("/api/reminders/messages")
                        .param("status", "UNREAD")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2));

        // 4. 查看 msg1 详情，自动标为已读
        mockMvc.perform(get("/api/reminders/messages/" + msg1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(msg1.getId()))
                .andExpect(jsonPath("$.data.status").value("READ"))
                .andExpect(jsonPath("$.data.isRead").value(true));

        // 未读数应减为 1
        mockMvc.perform(get("/api/reminders/messages/unread-count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));

        // 5. 一键全部标为已读
        mockMvc.perform(put("/api/reminders/messages/read-all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));

        // 未读数归零
        mockMvc.perform(get("/api/reminders/messages/unread-count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));

        // 6. 删除单条消息
        mockMvc.perform(delete("/api/reminders/messages/" + msg1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reminders/messages/" + msg1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("测试定时扫描与待办提醒：提前量触发、重复扫描严格幂等拦截与日志追踪")
    void testScheduledReminderExecutionAndStrictIdempotency() throws Exception {
        String token = registerAndGetToken("sched_user_1", "Pass123456");

        // 1. 创建待办任务：计划截止时间为 10 分钟后
        LocalDateTime dueTime = LocalDateTime.now().plusMinutes(10);
        TodoTask task = new TodoTask();
        task.setUserId(1L);
        task.setTitle("提交季度财务报表");
        task.setDescription("在系统内完成第四季度预算收支核对");
        task.setPriority(3);
        task.setStatus("TODO");
        task.setDueAt(dueTime);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setDeleted(0);
        todoTaskMapper.insert(task);

        // 2. 创建提醒规则：提前 15 分钟触发
        CreateReminderRuleRequest req = new CreateReminderRuleRequest();
        req.setRuleName("财务待办提前15分钟提醒");
        req.setTriggerType("TODO_DUE");
        req.setAdvanceMinutes(15);
        req.setRepeatRule("DAILY");
        req.setStatus(1);

        mockMvc.perform(post("/api/reminders/rules")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // 3. 执行第一次扫描调度：由于当前时间在 dueTime - 15分钟 之后，应成功触发 1 条提醒
        int firstScanCount = reminderSchedulerService.scanAndTriggerReminders();
        assertEquals(1, firstScanCount, "首次扫描应生成 1 条待办提醒消息");

        // 验证站内消息已生成
        mockMvc.perform(get("/api/reminders/messages/unread-count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));

        mockMvc.perform(get("/api/reminders/messages")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].title").value(containsString("提交季度财务报表")))
                .andExpect(jsonPath("$.data.records[0].messageType").value("TODO"));

        // 验证执行日志记录了该幂等键
        LambdaQueryWrapper<ReminderExecutionLog> logQw = new LambdaQueryWrapper<>();
        logQw.eq(ReminderExecutionLog::getUserId, 1L);
        assertEquals(1, reminderExecutionLogMapper.selectCount(logQw), "应有且仅有 1 条成功执行的幂等日志");

        // 4. 重复执行扫描调度（模拟高并发/定时器重复轮询）：严格幂等拦截！
        int secondScanCount = reminderSchedulerService.scanAndTriggerReminders();
        assertEquals(0, secondScanCount, "二次扫描必须被幂等机制拦截，新消息数量应为 0");

        int thirdScanCount = reminderSchedulerService.scanAndTriggerReminders();
        assertEquals(0, thirdScanCount, "三次扫描必须被幂等机制拦截，新消息数量应为 0");

        // 站内消息依然只有 1 条，绝无重复发送
        mockMvc.perform(get("/api/reminders/messages/unread-count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("测试习惯打卡与目标到期提醒场景及打卡后豁免提醒")
    void testHabitAndGoalReminders() throws Exception {
        String token = registerAndGetToken("habit_goal_user", "Pass123456");

        LocalDate today = LocalDate.now();

        // 1. 创建进行中的习惯（今日尚未打卡）
        Habit habit = new Habit();
        habit.setUserId(1L);
        habit.setName("晨间晨跑5公里");
        habit.setFrequency("DAILY");
        habit.setTargetCount(1);
        habit.setStartDate(today.minusDays(10));
        habit.setStatus("ACTIVE");
        habit.setCreatedAt(LocalDateTime.now());
        habit.setUpdatedAt(LocalDateTime.now());
        habit.setDeleted(0);
        habitMapper.insert(habit);

        // 2. 创建进行中的目标（明天到期）
        Goal goal = new Goal();
        goal.setUserId(1L);
        goal.setTitle("完成全栈系统重构");
        goal.setStartDate(today.minusDays(20));
        goal.setDueDate(today.plusDays(1));
        goal.setStatus("IN_PROGRESS");
        goal.setProgress(new BigDecimal("85.00"));
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal.setDeleted(0);
        goalMapper.insert(goal);

        // 3. 创建习惯打卡提醒规则（remindAt 为过去的1分钟前，保证已到达触发时间）
        ReminderRule habitRule = new ReminderRule();
        habitRule.setUserId(1L);
        habitRule.setRuleName("晨跑习惯每日打卡提醒");
        habitRule.setTriggerType("HABIT_CHECKIN");
        habitRule.setRemindAt(LocalTime.now().minusMinutes(5));
        habitRule.setRepeatRule("DAILY");
        habitRule.setStatus(1);
        habitRule.setCreatedAt(LocalDateTime.now());
        habitRule.setUpdatedAt(LocalDateTime.now());
        habitRule.setDeleted(0);
        reminderRuleMapper.insert(habitRule);

        // 4. 创建目标到期提醒规则
        ReminderRule goalRule = new ReminderRule();
        goalRule.setUserId(1L);
        goalRule.setRuleName("重点目标临期预警");
        goalRule.setTriggerType("GOAL_DUE");
        goalRule.setAdvanceMinutes(3 * 24 * 60); // 提前3天
        goalRule.setRepeatRule("DAILY");
        goalRule.setStatus(1);
        goalRule.setCreatedAt(LocalDateTime.now());
        goalRule.setUpdatedAt(LocalDateTime.now());
        goalRule.setDeleted(0);
        reminderRuleMapper.insert(goalRule);

        // 5. 触发扫描，应同时生成习惯提醒与目标提醒（共 2 条）
        int triggered = reminderSchedulerService.scanAndTriggerReminders();
        assertEquals(2, triggered, "应生成 1 条习惯打卡提醒与 1 条目标到期提醒");

        // 验证消息类型
        mockMvc.perform(get("/api/reminders/messages")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2));

        // 再次扫描：幂等拦截
        assertEquals(0, reminderSchedulerService.scanAndTriggerReminders());
    }

    @Test
    @DisplayName("测试停用和过期规则不触发提醒")
    void testDisabledAndExpiredRulesDoNotTrigger() throws Exception {
        registerAndGetToken("inactive_user", "Pass123456");

        // 1. 创建停用的规则 (status = 0)
        ReminderRule disabledRule = new ReminderRule();
        disabledRule.setUserId(1L);
        disabledRule.setRuleName("已停用的自定义提醒");
        disabledRule.setTriggerType("CUSTOM");
        disabledRule.setRemindAt(LocalTime.now().minusMinutes(5));
        disabledRule.setStatus(0); // 停用
        disabledRule.setCreatedAt(LocalDateTime.now());
        disabledRule.setUpdatedAt(LocalDateTime.now());
        disabledRule.setDeleted(0);
        reminderRuleMapper.insert(disabledRule);

        // 2. 创建过期的规则 (endDate 早于今天)
        ReminderRule expiredRule = new ReminderRule();
        expiredRule.setUserId(1L);
        expiredRule.setRuleName("已过期的自定义提醒");
        expiredRule.setTriggerType("CUSTOM");
        expiredRule.setRemindAt(LocalTime.now().minusMinutes(5));
        expiredRule.setEndDate(LocalDate.now().minusDays(1));
        expiredRule.setStatus(1);
        expiredRule.setCreatedAt(LocalDateTime.now());
        expiredRule.setUpdatedAt(LocalDateTime.now());
        expiredRule.setDeleted(0);
        reminderRuleMapper.insert(expiredRule);

        // 执行扫描：不应产生任何消息
        int count = reminderSchedulerService.scanAndTriggerReminders();
        assertEquals(0, count, "已停用或已过期的规则不得生成提醒消息");
    }

    @Test
    @DisplayName("测试跨用户提醒规则与消息强数据隔离")
    void testCrossUserIsolationForReminders() throws Exception {
        String tokenA = registerAndGetToken("user_a", "Pass123456");
        String tokenB = registerAndGetToken("user_b", "Pass123456");

        // 1. 用户 A 创建一条规则与一条消息
        CreateReminderRuleRequest req = new CreateReminderRuleRequest();
        req.setRuleName("用户A的私人提醒规则");
        req.setTriggerType("CUSTOM");
        req.setStatus(1);

        MvcResult ruleRes = mockMvc.perform(post("/api/reminders/rules")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        Long ruleIdA = objectMapper.readTree(ruleRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        ReminderMessage msgA = new ReminderMessage();
        msgA.setUserId(1L);
        msgA.setTitle("用户A的秘密通知");
        msgA.setContent("这是用户A的独享隐私数据");
        msgA.setMessageType("SYSTEM");
        msgA.setStatus("SENT");
        msgA.setCreatedAt(LocalDateTime.now());
        msgA.setDeleted(0);
        reminderMessageMapper.insert(msgA);

        // 2. 用户 B 尝试访问用户 A 的规则 -> 404
        mockMvc.perform(get("/api/reminders/rules/" + ruleIdA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 3. 用户 B 尝试修改/停用用户 A 的规则 -> 404
        mockMvc.perform(put("/api/reminders/rules/" + ruleIdA + "/status")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 4. 用户 B 尝试查看用户 A 的消息 -> 404
        mockMvc.perform(get("/api/reminders/messages/" + msgA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 5. 用户 B 尝试标记用户 A 的消息为已读 -> 404
        mockMvc.perform(put("/api/reminders/messages/" + msgA.getId() + "/read")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 6. 用户 B 查询消息列表与未读数 -> 0 条
        mockMvc.perform(get("/api/reminders/messages/unread-count")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));

        mockMvc.perform(get("/api/reminders/messages")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }
}
