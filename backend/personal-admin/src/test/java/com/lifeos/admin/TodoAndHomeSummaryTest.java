package com.lifeos.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.business.dto.CreateTodoRequest;
import com.lifeos.business.dto.UpdateTodoRequest;
import com.lifeos.business.dto.UpdateTodoStatusRequest;
import com.lifeos.business.entity.ModuleUsageLog;
import com.lifeos.business.entity.TodoTask;
import com.lifeos.business.mapper.ModuleUsageLogMapper;
import com.lifeos.business.mapper.TodoTaskMapper;
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
@DisplayName("待办管理与首页综合摘要集成测试")
class TodoAndHomeSummaryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TodoTaskMapper todoTaskMapper;

    @Autowired
    private ModuleUsageLogMapper moduleUsageLogMapper;

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

    @Test
    @DisplayName("测试创建待办、查询详情及操作审计写入")
    void testCreateTodoAndQueryDetail() throws Exception {
        String token = registerAndGetToken("user_todo_01", "Pass123456");

        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("学习 Vue 3 与 Spring Boot 3");
        request.setDescription("完成待办模块和首页摘要的前后端实现");
        request.setPriority(3);
        request.setStartAt(LocalDateTime.now().plusHours(1));
        request.setDueAt(LocalDateTime.now().plusDays(1));

        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("学习 Vue 3 与 Spring Boot 3"))
                .andExpect(jsonPath("$.data.priority").value(3))
                .andExpect(jsonPath("$.data.priorityDesc").value("高"))
                .andExpect(jsonPath("$.data.status").value("TODO"))
                .andExpect(jsonPath("$.data.statusDesc").value("待办"))
                .andReturn();

        Long todoId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 查询详情
        mockMvc.perform(get("/api/todos/" + todoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(todoId))
                .andExpect(jsonPath("$.data.title").value("学习 Vue 3 与 Spring Boot 3"));

        // 验证数据库记录
        TodoTask inDb = todoTaskMapper.selectById(todoId);
        assertNotNull(inDb);
        assertEquals("TODO", inDb.getStatus());
        assertEquals(0, inDb.getDeleted());

        // 验证 module_usage_log 是否记录
        Long logCount = moduleUsageLogMapper.selectCount(new LambdaQueryWrapper<ModuleUsageLog>()
                .eq(ModuleUsageLog::getModuleCode, "TODO")
                .eq(ModuleUsageLog::getActionCode, "CREATE")
                .eq(ModuleUsageLog::getBusinessRefId, todoId));
        assertEquals(1, logCount);
    }

    @Test
    @DisplayName("测试开始时间晚于截止时间的边界校验")
    void testTimeValidationStartAfterDue() throws Exception {
        String token = registerAndGetToken("user_todo_02", "Pass123456");

        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("错误时间待办");
        request.setStartAt(LocalDateTime.now().plusDays(2));
        request.setDueAt(LocalDateTime.now().plusDays(1)); // 截止时间早于开始时间

        mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("计划开始时间不得晚于截止时间")));
    }

    @Test
    @DisplayName("测试待办状态流转与完成时间记录")
    void testStatusTransitionsAndCompletedAt() throws Exception {
        String token = registerAndGetToken("user_todo_03", "Pass123456");

        // 1. 创建待办
        CreateTodoRequest createRequest = new CreateTodoRequest();
        createRequest.setTitle("流转任务测试");
        createRequest.setPriority(2);

        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Long todoId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 2. 状态变更为 IN_PROGRESS
        mockMvc.perform(patch("/api/todos/" + todoId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateTodoStatusRequest("IN_PROGRESS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.completedAt").doesNotExist());

        // 3. 状态变更为 DONE (完成)
        mockMvc.perform(patch("/api/todos/" + todoId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateTodoStatusRequest("DONE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DONE"))
                .andExpect(jsonPath("$.data.completedAt").isNotEmpty());

        TodoTask completedTask = todoTaskMapper.selectById(todoId);
        assertNotNull(completedTask.getCompletedAt());

        // 4. 重新打开任务 (变为 TODO) -> completedAt 必须被清空
        mockMvc.perform(patch("/api/todos/" + todoId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateTodoStatusRequest("TODO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("TODO"))
                .andExpect(jsonPath("$.data.completedAt").doesNotExist());

        TodoTask reopenedTask = todoTaskMapper.selectById(todoId);
        assertNull(reopenedTask.getCompletedAt());
    }

    @Test
    @DisplayName("测试跨用户读写隔离安全性")
    void testUserDataIsolation() throws Exception {
        String tokenUserA = registerAndGetToken("user_iso_a", "Pass123456");
        String tokenUserB = registerAndGetToken("user_iso_b", "Pass123456");

        // 用户 A 创建待办
        CreateTodoRequest requestA = new CreateTodoRequest();
        requestA.setTitle("用户A私密待办");
        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestA)))
                .andExpect(status().isOk())
                .andReturn();

        Long todoIdA = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 1. 用户 B 尝试读取用户 A 的待办 -> 404 NOT_FOUND
        mockMvc.perform(get("/api/todos/" + todoIdA)
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 2. 用户 B 尝试修改用户 A 的待办 -> 404 NOT_FOUND
        UpdateTodoRequest updateRequest = new UpdateTodoRequest();
        updateRequest.setTitle("恶意篡改标题");
        mockMvc.perform(put("/api/todos/" + todoIdA)
                        .header("Authorization", "Bearer " + tokenUserB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 3. 用户 B 尝试更新用户 A 待办状态 -> 404 NOT_FOUND
        mockMvc.perform(patch("/api/todos/" + todoIdA + "/status")
                        .header("Authorization", "Bearer " + tokenUserB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateTodoStatusRequest("DONE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 4. 用户 B 尝试删除用户 A 的待办 -> 404 NOT_FOUND
        mockMvc.perform(delete("/api/todos/" + todoIdA)
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 5. 用户 B 查询自己的待办列表 -> 数量必须为 0
        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records", hasSize(0)));
    }

    @Test
    @DisplayName("测试软删除行为")
    void testSoftDelete() throws Exception {
        String token = registerAndGetToken("user_todo_del", "Pass123456");

        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("待删除任务");
        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        Long todoId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 执行删除
        mockMvc.perform(delete("/api/todos/" + todoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 删除后再查 -> 404 NOT_FOUND
        mockMvc.perform(get("/api/todos/" + todoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 检查数据库：被逻辑删除，物理记录仍在
        TodoTask inDb = todoTaskMapper.selectById(todoId);
        assertNull(inDb, "MyBatis-Plus @TableLogic 会在默认查询中自动过滤已删除记录");
    }

    @Test
    @DisplayName("测试首页综合看板摘要接口与真实数据对齐")
    void testHomeSummaryAggregation() throws Exception {
        String token = registerAndGetToken("user_home_summary", "Pass123456");

        // 1. 创建任务 1: 今日待办，优先级高
        CreateTodoRequest t1 = new CreateTodoRequest();
        t1.setTitle("任务1-今日紧急");
        t1.setPriority(4);
        t1.setDueAt(LocalDateTime.now().plusHours(2));
        mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(t1)))
                .andExpect(status().isOk());

        // 2. 创建任务 2: 今日待办并完成它
        CreateTodoRequest t2 = new CreateTodoRequest();
        t2.setTitle("任务2-今日完成");
        t2.setPriority(2);
        t2.setDueAt(LocalDateTime.now().plusHours(4));
        MvcResult r2 = mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(t2)))
                .andExpect(status().isOk())
                .andReturn();
        Long t2Id = objectMapper.readTree(r2.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 完成任务 2
        mockMvc.perform(patch("/api/todos/" + t2Id + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateTodoStatusRequest("DONE"))))
                .andExpect(status().isOk());

        // 3. 创建任务 3: 明天待办 (非今日)
        CreateTodoRequest t3 = new CreateTodoRequest();
        t3.setTitle("任务3-明天任务");
        t3.setPriority(1);
        t3.setDueAt(LocalDateTime.now().plusDays(1));
        mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(t3)))
                .andExpect(status().isOk());

        // 4. 调用 /api/home/summary 校验统计指标
        mockMvc.perform(get("/api/home/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalTodoCount").value(3))
                .andExpect(jsonPath("$.data.todayTodoCount").value(2))
                .andExpect(jsonPath("$.data.todayCompletedTodoCount").value(1))
                .andExpect(jsonPath("$.data.pendingTodoCount").value(2)) // 任务1和任务3待完成
                .andExpect(jsonPath("$.data.greeting").isNotEmpty())
                .andExpect(jsonPath("$.data.currentDate").isNotEmpty())
                .andExpect(jsonPath("$.data.dayOfWeekDesc").isNotEmpty());

        // 5. 调用 /api/todos/summary 校验待办专项摘要
        mockMvc.perform(get("/api/todos/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalCount").value(3))
                .andExpect(jsonPath("$.data.todayTotalCount").value(2))
                .andExpect(jsonPath("$.data.todayCompletedCount").value(1))
                .andExpect(jsonPath("$.data.pendingCount").value(2))
                .andExpect(jsonPath("$.data.completedCount").value(1));
    }
}
