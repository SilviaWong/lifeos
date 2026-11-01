package com.lifeos.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.business.dto.CheckinHabitRequest;
import com.lifeos.business.dto.CreateHabitRequest;
import com.lifeos.business.dto.UpdateHabitRequest;
import com.lifeos.business.dto.UpdateHabitStatusRequest;
import com.lifeos.business.entity.HabitCheckin;
import com.lifeos.business.entity.ModuleUsageLog;
import com.lifeos.business.mapper.HabitCheckinMapper;
import com.lifeos.business.mapper.ModuleUsageLogMapper;
import com.lifeos.system.dto.LoginDTO;
import com.lifeos.system.dto.RegisterDTO;
import com.lifeos.system.entity.SysUser;
import com.lifeos.system.mapper.SysUserMapper;
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
@DisplayName("习惯打卡 CRUD、幂等性约束、连续天数与月历统计集成测试")
class HabitCheckinTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HabitCheckinMapper habitCheckinMapper;

    @Autowired
    private com.lifeos.business.mapper.HabitMapper habitMapper;

    @Autowired
    private ModuleUsageLogMapper moduleUsageLogMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        habitCheckinMapper.delete(null);
        habitMapper.delete(null);
    }

    private String registerAndGetToken(String username, String password) throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(username);
        registerDTO.setPassword(password);
        registerDTO.setPhone("137" + System.currentTimeMillis() % 100000000);
        registerDTO.setNickname("习惯打卡者" + username);

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
    @DisplayName("测试习惯增删改查、状态流转与已暂停习惯打卡拒绝")
    void testHabitCrudAndStatusFlow() throws Exception {
        String token = registerAndGetToken("habit_user_01", "Pass123456");

        // 1. 创建习惯：每天晨跑 5 公里
        CreateHabitRequest createReq = new CreateHabitRequest();
        createReq.setName("每天晨跑 5 公里");
        createReq.setDescription("健康生活，从跑步开始");
        createReq.setFrequency("DAILY");
        createReq.setTargetCount(1);
        createReq.setStartDate(LocalDate.now());
        createReq.setReminderTime("07:00:00");

        MvcResult createResult = mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("每天晨跑 5 公里"))
                .andExpect(jsonPath("$.data.frequency").value("DAILY"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.todayChecked").value(false))
                .andExpect(jsonPath("$.data.currentStreak").value(0))
                .andReturn();

        long habitId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 2. 编辑习惯
        UpdateHabitRequest updateReq = new UpdateHabitRequest();
        updateReq.setName("每天晨跑 6 公里（进阶）");
        updateReq.setDescription("加速配速");
        updateReq.setFrequency("DAILY");
        updateReq.setTargetCount(1);
        updateReq.setStartDate(LocalDate.now());
        updateReq.setReminderTime("06:45:00");

        mockMvc.perform(put("/api/habits/" + habitId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("每天晨跑 6 公里（进阶）"));

        // 3. 将习惯置为 PAUSED（暂停）
        mockMvc.perform(put("/api/habits/" + habitId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateHabitStatusRequest("PAUSED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PAUSED"));

        // 4. 暂停状态下打卡应被拦截
        mockMvc.perform(post("/api/habits/" + habitId + "/checkins")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)))
                .andExpect(jsonPath("$.message").value(containsString("无法打卡")));

        // 5. 恢复为 ACTIVE 并打卡
        mockMvc.perform(put("/api/habits/" + habitId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateHabitStatusRequest("ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(post("/api/habits/" + habitId + "/checkins")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckinHabitRequest(LocalDate.now(), "跑完了6公里，神清气爽！"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayChecked").value(true))
                .andExpect(jsonPath("$.data.currentStreak").value(1))
                .andExpect(jsonPath("$.data.totalCheckins").value(1));

        // 6. 删除习惯
        mockMvc.perform(delete("/api/habits/" + habitId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 7. 再次查询详情应返回 404
        mockMvc.perform(get("/api/habits/" + habitId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)));
    }

    @Test
    @DisplayName("测试同一天打卡幂等性、禁止未来日期打卡与取消后重新打卡")
    void testCheckinIdempotencyAndFutureDateRejection() throws Exception {
        String token = registerAndGetToken("habit_user_02", "Pass123456");

        // 1. 创建习惯
        CreateHabitRequest createReq = new CreateHabitRequest();
        createReq.setName("每日饮水 2000ml");
        createReq.setFrequency("DAILY");
        createReq.setStartDate(LocalDate.now().minusDays(5));

        MvcResult createResult = mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();

        long habitId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 2. 尝试在未来日期打卡（明日），应被严格拒绝
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        mockMvc.perform(post("/api/habits/" + habitId + "/checkins")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckinHabitRequest(tomorrow, "穿越打卡"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)))
                .andExpect(jsonPath("$.message").value(containsString("不能在未来日期打卡")));

        // 3. 正常打卡今天
        mockMvc.perform(post("/api/habits/" + habitId + "/checkins")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckinHabitRequest(LocalDate.now(), "喝水充足"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayChecked").value(true));

        // 4. 重复打卡今天，应被拦截（幂等性）
        mockMvc.perform(post("/api/habits/" + habitId + "/checkins")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckinHabitRequest(LocalDate.now(), "重复提交"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)))
                .andExpect(jsonPath("$.message").value(containsString("请勿重复打卡")));

        // 5. 取消今日打卡
        mockMvc.perform(delete("/api/habits/" + habitId + "/checkins")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayChecked").value(false))
                .andExpect(jsonPath("$.data.totalCheckins").value(0));

        // 6. 取消后再次打卡今天，应能重新激活成功且不发生数据库唯一键冲突
        mockMvc.perform(post("/api/habits/" + habitId + "/checkins")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckinHabitRequest(LocalDate.now(), "重新打卡"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayChecked").value(true))
                .andExpect(jsonPath("$.data.totalCheckins").value(1));

        // 7. 再次重复打卡依然被拒绝
        mockMvc.perform(post("/api/habits/" + habitId + "/checkins")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)))
                .andExpect(jsonPath("$.message").value(containsString("请勿重复打卡")));
    }

    @Test
    @DisplayName("测试连续打卡天数计算、跨月跨年处理与历史最大连续打卡数")
    void testStreakCalculationAndCrossMonth() throws Exception {
        String token = registerAndGetToken("habit_user_03", "Pass123456");

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "habit_user_03")
        );
        assertNotNull(user);

        // 创建习惯：开始日期定在 10 天前
        LocalDate startDate = LocalDate.now().minusDays(10);
        CreateHabitRequest createReq = new CreateHabitRequest();
        createReq.setName("每日阅读 30 分钟");
        createReq.setFrequency("DAILY");
        createReq.setStartDate(startDate);

        MvcResult createResult = mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();

        long habitId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 插入连续历史打卡：连续打卡 3 天（today-3, today-2, today-1）
        LocalDate d3 = LocalDate.now().minusDays(3);
        LocalDate d2 = LocalDate.now().minusDays(2);
        LocalDate d1 = LocalDate.now().minusDays(1);

        habitCheckinMapper.insert(new HabitCheckin(user.getId(), habitId, d3, "打卡1", 0));
        habitCheckinMapper.insert(new HabitCheckin(user.getId(), habitId, d2, "打卡2", 0));
        habitCheckinMapper.insert(new HabitCheckin(user.getId(), habitId, d1, "打卡3", 0));

        // 今日尚未打卡，但昨日打卡了，连续打卡尚未中断 -> currentStreak 应为 3
        mockMvc.perform(get("/api/habits/" + habitId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayChecked").value(false))
                .andExpect(jsonPath("$.data.currentStreak").value(3))
                .andExpect(jsonPath("$.data.maxStreak").value(3))
                .andExpect(jsonPath("$.data.totalCheckins").value(3));

        // 打卡今日 -> currentStreak 应跃升为 4
        mockMvc.perform(post("/api/habits/" + habitId + "/checkins")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckinHabitRequest(LocalDate.now(), "今日完成阅读"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayChecked").value(true))
                .andExpect(jsonPath("$.data.currentStreak").value(4))
                .andExpect(jsonPath("$.data.maxStreak").value(4))
                .andExpect(jsonPath("$.data.totalCheckins").value(4));
    }

    @Test
    @DisplayName("测试月历打卡数据、打卡流水明细分页与完成率准确性")
    void testHabitCalendarAndPagination() throws Exception {
        String token = registerAndGetToken("habit_user_04", "Pass123456");

        CreateHabitRequest createReq = new CreateHabitRequest();
        createReq.setName("练习吉他");
        createReq.setFrequency("DAILY");
        createReq.setStartDate(LocalDate.now().minusDays(20));

        MvcResult createResult = mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();

        long habitId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 打卡今日
        mockMvc.perform(post("/api/habits/" + habitId + "/checkins")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckinHabitRequest(LocalDate.now(), "练习卡农"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 查询月历
        LocalDate now = LocalDate.now();
        mockMvc.perform(get("/api/habits/" + habitId + "/calendar")
                        .param("year", String.valueOf(now.getYear()))
                        .param("month", String.valueOf(now.getMonthValue()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.habitId").value(habitId))
                .andExpect(jsonPath("$.data.month").value(now.getMonthValue()))
                .andExpect(jsonPath("$.data.monthCheckins").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.checkinDates", hasItem(now.toString())));

        // 查询打卡流水明细
        mockMvc.perform(get("/api/habits/" + habitId + "/checkins")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].note").value("练习卡农"));
    }

    @Test
    @DisplayName("测试跨用户数据隔离：无法查看、编辑、打卡或删除其他用户的习惯")
    void testCrossUserDataIsolation() throws Exception {
        String tokenA = registerAndGetToken("habit_user_A", "Pass123456");
        String tokenB = registerAndGetToken("habit_user_B", "Pass123456");

        // 用户 A 创建习惯
        CreateHabitRequest createReq = new CreateHabitRequest();
        createReq.setName("用户A的私有习惯");
        createReq.setStartDate(LocalDate.now());

        MvcResult createResult = mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();

        long habitIdA = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 用户 B 试图访问 A 的习惯详情 -> 404
        mockMvc.perform(get("/api/habits/" + habitIdA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)));

        // 用户 B 试图对 A 的习惯打卡 -> 404 拒绝
        mockMvc.perform(post("/api/habits/" + habitIdA + "/checkins")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)));

        // 用户 B 试图修改 A 的习惯 -> 404 拒绝
        UpdateHabitRequest updateReq = new UpdateHabitRequest();
        updateReq.setName("恶意篡改");
        updateReq.setStartDate(LocalDate.now());
        mockMvc.perform(put("/api/habits/" + habitIdA)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)));

        // 用户 B 试图删除 A 的习惯 -> 404 拒绝
        mockMvc.perform(delete("/api/habits/" + habitIdA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)));
    }

    @Test
    @DisplayName("测试习惯总览统计、首页聚合接口联动与审计日志落地")
    void testHabitSummaryAndHomeSummaryIntegration() throws Exception {
        String token = registerAndGetToken("habit_user_summary", "Pass123456");

        // 1. 创建 2 个习惯
        CreateHabitRequest req1 = new CreateHabitRequest();
        req1.setName("早起早睡");
        req1.setStartDate(LocalDate.now());
        MvcResult res1 = mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andReturn();
        long id1 = objectMapper.readTree(res1.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateHabitRequest req2 = new CreateHabitRequest();
        req2.setName("写代码复盘");
        req2.setStartDate(LocalDate.now());
        mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isOk());

        // 2. 对习惯 1 打卡
        mockMvc.perform(post("/api/habits/" + id1 + "/checkins")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 3. 查询习惯模块总览
        mockMvc.perform(get("/api/habits/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalHabits").value(2))
                .andExpect(jsonPath("$.data.activeHabits").value(2))
                .andExpect(jsonPath("$.data.todayCheckedHabits").value(1));

        // 4. 查询综合首页看板，验证 todayHabitCheckinCount 真实聚合
        mockMvc.perform(get("/api/home/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayHabitCheckinCount").value(1));

        // 5. 验证 module_usage_log 记录了 HABIT 模块的 CREATE 和 CHECKIN 动作
        Long logCount = moduleUsageLogMapper.selectCount(
                new LambdaQueryWrapper<ModuleUsageLog>()
                        .eq(ModuleUsageLog::getModuleCode, "HABIT")
        );
        assertTrue(logCount >= 3, "ModuleUsageLog 中应包含习惯操作审计记录");
    }
}
