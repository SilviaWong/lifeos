package com.lifeos.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.business.dto.AbortPomodoroRequest;
import com.lifeos.business.dto.CreateGoalRequest;
import com.lifeos.business.dto.CreateTodoRequest;
import com.lifeos.business.dto.FinishPomodoroRequest;
import com.lifeos.business.dto.StartPomodoroRequest;
import com.lifeos.business.dto.UpdatePomodoroSettingRequest;
import com.lifeos.business.entity.ModuleUsageLog;
import com.lifeos.business.entity.PomodoroRecord;
import com.lifeos.business.mapper.ModuleUsageLogMapper;
import com.lifeos.business.mapper.PomodoroRecordMapper;
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
@DisplayName("番茄钟专注会话状态流转、时长校验与数据隔离测试")
class PomodoroSessionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PomodoroRecordMapper pomodoroRecordMapper;

    @Autowired
    private ModuleUsageLogMapper moduleUsageLogMapper;

    @Autowired
    private com.lifeos.system.mapper.SysUserMapper sysUserMapper;

    private String registerAndGetToken(String username, String password) throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(username);
        registerDTO.setPassword(password);
        registerDTO.setPhone("136" + System.currentTimeMillis() % 100000000);
        registerDTO.setNickname("专注者" + username);

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

    @Test
    @DisplayName("测试开启会话、暂停、继续与单用户只能存在一个 RUNNING 会话校验")
    void testStartPauseResumeAndSingleActiveSession() throws Exception {
        String token = registerAndGetToken("pomo_user_01", "Pass123456");

        // 1. 开启一个 25 分钟 (1500 秒) 专注会话
        StartPomodoroRequest startReq = new StartPomodoroRequest();
        startReq.setSessionType("FOCUS");
        startReq.setPlannedSeconds(1500);

        MvcResult startResult = mockMvc.perform(post("/api/pomodoros/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.sessionType").value("FOCUS"))
                .andExpect(jsonPath("$.data.plannedSeconds").value(1500))
                .andReturn();

        long sessionId = objectMapper.readTree(startResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 2. 再次尝试开启新会话，预期被拒绝 (单用户最多 1 个未结束会话)
        mockMvc.perform(post("/api/pomodoros/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("当前已有进行中的番茄钟会话")));

        // 3. 暂停该会话
        mockMvc.perform(post("/api/pomodoros/" + sessionId + "/pause")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PAUSED"));

        // 4. 重复暂停 (幂等返回)
        mockMvc.perform(post("/api/pomodoros/" + sessionId + "/pause")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PAUSED"));

        // 5. 恢复继续该会话
        mockMvc.perform(post("/api/pomodoros/" + sessionId + "/resume")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("RUNNING"));

        // 6. 验证当前活动会话查询
        mockMvc.perform(get("/api/pomodoros/current")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(sessionId))
                .andExpect(jsonPath("$.data.status").value("RUNNING"));
    }

    @Test
    @DisplayName("测试完成专注会话与服务端真实时长校验防护（拒绝虚假客户端上报时长）")
    void testFinishSessionWithServerTimeGuard() throws Exception {
        String token = registerAndGetToken("pomo_user_02", "Pass123456");

        // 1. 开启一个专注会话
        StartPomodoroRequest startReq = new StartPomodoroRequest();
        startReq.setSessionType("FOCUS");
        startReq.setPlannedSeconds(1500);

        MvcResult startResult = mockMvc.perform(post("/api/pomodoros/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isOk())
                .andReturn();

        long sessionId = objectMapper.readTree(startResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 2. 客户端立即请求完成，谎称已经专注了 1500 秒
        FinishPomodoroRequest fakeFinishReq = new FinishPomodoroRequest(1500);

        MvcResult finishResult = mockMvc.perform(post("/api/pomodoros/" + sessionId + "/finish")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeFinishReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.endReason").value("NORMAL_FINISH"))
                .andReturn();

        int actualSeconds = objectMapper.readTree(finishResult.getResponse().getContentAsString())
                .path("data").path("actualSeconds").asInt();

        // 服务端校验保障：由于刚创建会话，真实服务器时间差仅 0~1 秒，即使客户端上报 1500 秒，实际记录值也被限制在服务端有效时长内（<= 16秒）
        assertTrue(actualSeconds <= 16, "实际专注秒数必须受服务端真实时间差约束，防止虚假客户端时长写入");

        // 3. 重复请求完成（幂等保障，不重复记录或报错）
        mockMvc.perform(post("/api/pomodoros/" + sessionId + "/finish")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeFinishReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // 4. 验证操作日志审计
        Long finishLogCount = moduleUsageLogMapper.selectCount(new LambdaQueryWrapper<ModuleUsageLog>()
                .eq(ModuleUsageLog::getModuleCode, "POMODORO")
                .eq(ModuleUsageLog::getActionCode, "FINISH"));
        assertTrue(finishLogCount >= 1);
    }

    @Test
    @DisplayName("测试中途放弃中止会话与关联目标/待办")
    void testAbortSessionWithGoalAndTodoAssociation() throws Exception {
        String token = registerAndGetToken("pomo_user_03", "Pass123456");

        // 1. 创建目标
        CreateGoalRequest goalReq = new CreateGoalRequest();
        goalReq.setTitle("冲刺全栈架构师");
        goalReq.setStartDate(LocalDate.now());
        goalReq.setDueDate(LocalDate.now().plusMonths(1));
        MvcResult goalRes = mockMvc.perform(post("/api/goals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(goalReq))).andExpect(status().isOk()).andReturn();
        long goalId = objectMapper.readTree(goalRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. 创建待办关联目标
        CreateTodoRequest todoReq = new CreateTodoRequest();
        todoReq.setTitle("精读 Spring 核心源码");
        todoReq.setGoalId(goalId);
        MvcResult todoRes = mockMvc.perform(post("/api/todos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(todoReq))).andExpect(status().isOk()).andReturn();
        long todoId = objectMapper.readTree(todoRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 3. 开启专注会话并关联待办
        StartPomodoroRequest pomoReq = new StartPomodoroRequest();
        pomoReq.setPlannedSeconds(1800);
        pomoReq.setTodoId(todoId);

        MvcResult pomoRes = mockMvc.perform(post("/api/pomodoros/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pomoReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todoId").value(todoId))
                .andExpect(jsonPath("$.data.todoTitle").value("精读 Spring 核心源码"))
                .andExpect(jsonPath("$.data.goalId").value(goalId))
                .andExpect(jsonPath("$.data.goalTitle").value("冲刺全栈架构师"))
                .andReturn();
        long pomoId = objectMapper.readTree(pomoRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 4. 用户临时被打断，中止会话
        AbortPomodoroRequest abortReq = new AbortPomodoroRequest("紧急会议打断", 120);

        mockMvc.perform(post("/api/pomodoros/" + pomoId + "/abort")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abortReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("ABORTED"))
                .andExpect(jsonPath("$.data.endReason").value("紧急会议打断"));

        // 5. 中止后当前活动会话应为空，且可以开启下一个新会话
        mockMvc.perform(get("/api/pomodoros/current")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("测试跨用户会话严格隔离（用户 B 不能控制或篡改用户 A 的番茄钟）")
    void testCrossUserDataIsolation() throws Exception {
        String tokenUserA = registerAndGetToken("pomo_alice", "Pass123456");
        String tokenUserB = registerAndGetToken("pomo_bob", "Pass123456");

        // Alice 开启一个会话
        StartPomodoroRequest startReq = new StartPomodoroRequest();
        startReq.setPlannedSeconds(1500);
        MvcResult res = mockMvc.perform(post("/api/pomodoros/start")
                .header("Authorization", "Bearer " + tokenUserA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startReq))).andExpect(status().isOk()).andReturn();
        long aliceSessionId = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Bob 尝试暂停 Alice 的会话，预期返回 404
        mockMvc.perform(post("/api/pomodoros/" + aliceSessionId + "/pause")
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // Bob 尝试完成 Alice 的会话，预期返回 404
        mockMvc.perform(post("/api/pomodoros/" + aliceSessionId + "/finish")
                        .header("Authorization", "Bearer " + tokenUserB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FinishPomodoroRequest(1500))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // Bob 尝试中止 Alice 的会话，预期返回 404
        mockMvc.perform(post("/api/pomodoros/" + aliceSessionId + "/abort")
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // Bob 查询记录列表，无法看到 Alice 的记录
        mockMvc.perform(get("/api/pomodoros/records")
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records", hasSize(0)));
    }

    @Test
    @DisplayName("测试番茄钟偏好配置获取与保存")
    void testPomodoroSettingsManagement() throws Exception {
        String token = registerAndGetToken("pomo_setting_user", "Pass123456");

        // 1. 获取默认配置
        mockMvc.perform(get("/api/pomodoros/settings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.focusDuration").value(25))
                .andExpect(jsonPath("$.data.shortBreakDuration").value(5))
                .andExpect(jsonPath("$.data.longBreakDuration").value(15))
                .andExpect(jsonPath("$.data.longBreakInterval").value(4))
                .andExpect(jsonPath("$.data.autoStartBreaks").value(false));

        // 2. 修改配置 (专注 30 分钟，短休息 6 分钟，长休息 20 分钟，长休息间隔 3 次，自动开始休息)
        UpdatePomodoroSettingRequest updateReq = new UpdatePomodoroSettingRequest();
        updateReq.setFocusDuration(30);
        updateReq.setShortBreakDuration(6);
        updateReq.setLongBreakDuration(20);
        updateReq.setLongBreakInterval(3);
        updateReq.setAutoStartBreaks(true);
        updateReq.setAutoStartPomodoros(false);

        mockMvc.perform(put("/api/pomodoros/settings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.focusDuration").value(30))
                .andExpect(jsonPath("$.data.shortBreakDuration").value(6))
                .andExpect(jsonPath("$.data.longBreakDuration").value(20))
                .andExpect(jsonPath("$.data.longBreakInterval").value(3))
                .andExpect(jsonPath("$.data.autoStartBreaks").value(true));

        // 3. 再次查询确认持久化生效
        mockMvc.perform(get("/api/pomodoros/settings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.focusDuration").value(30))
                .andExpect(jsonPath("$.data.autoStartBreaks").value(true));
    }

    @Test
    @DisplayName("测试今日与本周专注统计汇总")
    void testPomodoroSummaryCalculation() throws Exception {
        String token = registerAndGetToken("pomo_summary_user", "Pass123456");

        // 初始统计应全为 0
        mockMvc.perform(get("/api/pomodoros/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayCompletedCount").value(0))
                .andExpect(jsonPath("$.data.todayFocusMinutes").value(0));

        // 从数据库查询当前注册用户的真实 ID
        com.lifeos.system.entity.SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<com.lifeos.system.entity.SysUser>()
                        .eq(com.lifeos.system.entity.SysUser::getUsername, "pomo_summary_user")
        );
        assertNotNull(user);
        long uid = user.getId();

        // 模拟直接在数据库插入已完成的历史记录（避免测试等待 25 分钟）
        PomodoroRecord r1 = new PomodoroRecord();
        r1.setUserId(uid);
        r1.setSessionType("FOCUS");
        r1.setPlannedSeconds(1500);
        r1.setActualSeconds(1500); // 25 分钟
        r1.setStatus("COMPLETED");
        r1.setStartedAt(LocalDateTime.now().minusHours(2));
        r1.setEndedAt(LocalDateTime.now().minusHours(2).plusMinutes(25));
        r1.setEndReason("NORMAL_FINISH");
        pomodoroRecordMapper.insert(r1);

        PomodoroRecord r2 = new PomodoroRecord();
        r2.setUserId(uid);
        r2.setSessionType("FOCUS");
        r2.setPlannedSeconds(1800);
        r2.setActualSeconds(1800); // 30 分钟
        r2.setStatus("COMPLETED");
        r2.setStartedAt(LocalDateTime.now().minusHours(1));
        r2.setEndedAt(LocalDateTime.now().minusHours(1).plusMinutes(30));
        r2.setEndReason("NORMAL_FINISH");
        pomodoroRecordMapper.insert(r2);

        // 再次获取统计
        mockMvc.perform(get("/api/pomodoros/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayCompletedCount").value(2))
                .andExpect(jsonPath("$.data.todayFocusMinutes").value(55))
                .andExpect(jsonPath("$.data.weekCompletedCount").value(2))
                .andExpect(jsonPath("$.data.weekFocusMinutes").value(55));
    }
}
