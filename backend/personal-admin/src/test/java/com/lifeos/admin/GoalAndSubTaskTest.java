package com.lifeos.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.business.dto.CreateGoalRequest;
import com.lifeos.business.dto.CreateGoalTaskRequest;
import com.lifeos.business.dto.CreateTodoRequest;
import com.lifeos.business.dto.UpdateGoalRequest;
import com.lifeos.business.dto.UpdateGoalStatusRequest;
import com.lifeos.business.dto.UpdateGoalTaskStatusRequest;
import com.lifeos.business.entity.Goal;
import com.lifeos.business.entity.GoalTask;
import com.lifeos.business.entity.ModuleUsageLog;
import com.lifeos.business.entity.TodoTask;
import com.lifeos.business.mapper.GoalMapper;
import com.lifeos.business.mapper.GoalTaskMapper;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("目标与子任务动态进度计算与数据隔离测试")
class GoalAndSubTaskTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GoalMapper goalMapper;

    @Autowired
    private GoalTaskMapper goalTaskMapper;

    @Autowired
    private TodoTaskMapper todoTaskMapper;

    @Autowired
    private ModuleUsageLogMapper moduleUsageLogMapper;

    private String registerAndGetToken(String username, String password) throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(username);
        registerDTO.setPassword(password);
        registerDTO.setPhone("137" + System.currentTimeMillis() % 100000000);
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
    @DisplayName("测试创建目标与无子任务时的「尚未拆解」进度状态")
    void testCreateGoalWithoutSubtasks() throws Exception {
        String token = registerAndGetToken("user_goal_01", "Pass123456");

        CreateGoalRequest request = new CreateGoalRequest();
        request.setTitle("掌握微服务架构与云原生实践");
        request.setDescription("系统性学习Spring Cloud Alibaba与Kubernetes");
        request.setStartDate(LocalDate.now());
        request.setDueDate(LocalDate.now().plusMonths(3));
        request.setPriority(1); // 1-高

        MvcResult result = mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("掌握微服务架构与云原生实践"))
                .andExpect(jsonPath("$.data.priorityDesc").value("高"))
                .andExpect(jsonPath("$.data.status").value("NOT_STARTED"))
                .andExpect(jsonPath("$.data.progress").value(0))
                .andExpect(jsonPath("$.data.progressText").value("尚未拆解"))
                .andExpect(jsonPath("$.data.totalTaskCount").value(0))
                .andReturn();

        Long goalId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 验证 module_usage_log
        Long logCount = moduleUsageLogMapper.selectCount(new LambdaQueryWrapper<ModuleUsageLog>()
                .eq(ModuleUsageLog::getModuleCode, "GOAL")
                .eq(ModuleUsageLog::getActionCode, "CREATE")
                .eq(ModuleUsageLog::getBusinessRefId, goalId));
        assertEquals(1, logCount);
    }

    @Test
    @DisplayName("测试开始日期晚于截止日期的边界校验")
    void testDateValidationStartAfterDue() throws Exception {
        String token = registerAndGetToken("user_goal_02", "Pass123456");

        CreateGoalRequest request = new CreateGoalRequest();
        request.setTitle("时间错误的目标");
        request.setStartDate(LocalDate.now().plusMonths(2));
        request.setDueDate(LocalDate.now().plusMonths(1));

        mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("开始日期不得晚于截止日期")));
    }

    @Test
    @DisplayName("测试服务端子任务驱动的进度计算完整生命周期（0% -> 50% -> 100% -> 重新打开 -> 删除恢复）")
    void testSubtaskProgressCalculationCycle() throws Exception {
        String token = registerAndGetToken("user_goal_03", "Pass123456");

        // 1. 创建目标
        CreateGoalRequest goalReq = new CreateGoalRequest();
        goalReq.setTitle("完成阶段6全套编码");
        goalReq.setPriority(1);

        MvcResult goalRes = mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq)))
                .andExpect(status().isOk())
                .andReturn();

        Long goalId = objectMapper.readTree(goalRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 2. 添加子任务 1
        CreateGoalTaskRequest taskReq1 = new CreateGoalTaskRequest();
        taskReq1.setTitle("编写后端Entity/Service/Controller");
        taskReq1.setSortNo(1);
        MvcResult t1Res = mockMvc.perform(post("/api/goals/" + goalId + "/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq1)))
                .andExpect(status().isOk())
                .andReturn();
        Long task1Id = objectMapper.readTree(t1Res.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 此时目标有 1 个任务，进度为 0%
        mockMvc.perform(get("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progress").value(0))
                .andExpect(jsonPath("$.data.progressText").value("0%"))
                .andExpect(jsonPath("$.data.totalTaskCount").value(1))
                .andExpect(jsonPath("$.data.completedTaskCount").value(0))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS")); // 自动推进为进行中

        // 3. 添加子任务 2
        CreateGoalTaskRequest taskReq2 = new CreateGoalTaskRequest();
        taskReq2.setTitle("编写前端页面与测试套件");
        taskReq2.setSortNo(2);
        MvcResult t2Res = mockMvc.perform(post("/api/goals/" + goalId + "/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq2)))
                .andExpect(status().isOk())
                .andReturn();
        Long task2Id = objectMapper.readTree(t2Res.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 4. 完成子任务 1 -> 进度自动变为 50%
        mockMvc.perform(patch("/api/goals/" + goalId + "/tasks/" + task1Id + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateGoalTaskStatusRequest("DONE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DONE"));

        mockMvc.perform(get("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progress").value(50.0))
                .andExpect(jsonPath("$.data.progressText").value("50%"))
                .andExpect(jsonPath("$.data.completedTaskCount").value(1))
                .andExpect(jsonPath("$.data.totalTaskCount").value(2));

        // 5. 完成子任务 2 -> 进度自动变为 100%
        mockMvc.perform(patch("/api/goals/" + goalId + "/tasks/" + task2Id + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateGoalTaskStatusRequest("DONE"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progress").value(100.0))
                .andExpect(jsonPath("$.data.progressText").value("100%"))
                .andExpect(jsonPath("$.data.completedTaskCount").value(2));

        // 6. 重新打开子任务 1 (重置为 TODO) -> 进度回到 50%
        mockMvc.perform(patch("/api/goals/" + goalId + "/tasks/" + task1Id + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateGoalTaskStatusRequest("TODO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("TODO"));

        mockMvc.perform(get("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progress").value(50.0))
                .andExpect(jsonPath("$.data.completedTaskCount").value(1));

        // 7. 删除子任务 2 (删除已完成的任务) -> 仅剩未完成的任务 1，进度回到 0%
        mockMvc.perform(delete("/api/goals/" + goalId + "/tasks/" + task2Id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progress").value(0))
                .andExpect(jsonPath("$.data.totalTaskCount").value(1))
                .andExpect(jsonPath("$.data.completedTaskCount").value(0));

        // 8. 删除子任务 1 -> 任务总数归零，进度重置为「尚未拆解」
        mockMvc.perform(delete("/api/goals/" + goalId + "/tasks/" + task1Id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progress").value(0))
                .andExpect(jsonPath("$.data.progressText").value("尚未拆解"))
                .andExpect(jsonPath("$.data.totalTaskCount").value(0));
    }

    @Test
    @DisplayName("测试目标删除时解除待办关联，绝不级联误删待办")
    void testGoalDeleteUnlinksTodoWithoutDeletingTodo() throws Exception {
        String token = registerAndGetToken("user_goal_unlink", "Pass123456");

        // 1. 创建目标
        CreateGoalRequest goalReq = new CreateGoalRequest();
        goalReq.setTitle("目标-关联测试");
        MvcResult gRes = mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq)))
                .andExpect(status().isOk())
                .andReturn();
        Long goalId = objectMapper.readTree(gRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. 为目标创建子任务
        CreateGoalTaskRequest taskReq = new CreateGoalTaskRequest();
        taskReq.setTitle("关联测试子任务");
        MvcResult tRes = mockMvc.perform(post("/api/goals/" + goalId + "/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isOk())
                .andReturn();
        Long taskId = objectMapper.readTree(tRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 3. 创建关联此目标的待办任务
        CreateTodoRequest todoReq = new CreateTodoRequest();
        todoReq.setTitle("受目标关联保护的待办");
        todoReq.setGoalId(goalId);
        MvcResult todoRes = mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.goalId").value(goalId))
                .andReturn();
        Long todoId = objectMapper.readTree(todoRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 4. 删除目标
        mockMvc.perform(delete("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 5. 校验目标已被软删除
        mockMvc.perform(get("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 6. 校验子任务已被删除
        GoalTask taskInDb = goalTaskMapper.selectById(taskId);
        assertNull(taskInDb, "目标下的子任务应随目标软删除");

        // 7. 核心业务规则验收：待办任务依然存在，但 goalId 已经被安全解绑置空
        TodoTask todoInDb = todoTaskMapper.selectById(todoId);
        assertNotNull(todoInDb, "待办任务绝不能被级联删除！");
        assertEquals(0, todoInDb.getDeleted(), "待办任务必须保持未删除状态");
        assertNull(todoInDb.getGoalId(), "待办任务的 goal_id 必须已被解除关联置为 null");

        // 接口查询待办详情依然正常可用
        mockMvc.perform(get("/api/todos/" + todoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(todoId))
                .andExpect(jsonPath("$.data.goalId").doesNotExist());
    }

    @Test
    @DisplayName("测试目标与子任务的跨用户数据隔离安全控制")
    void testGoalDataIsolation() throws Exception {
        String tokenA = registerAndGetToken("user_goal_iso_a", "Pass123456");
        String tokenB = registerAndGetToken("user_goal_iso_b", "Pass123456");

        // 用户 A 创建目标与子任务
        CreateGoalRequest gReq = new CreateGoalRequest();
        gReq.setTitle("用户A私密目标");
        MvcResult gRes = mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gReq)))
                .andExpect(status().isOk())
                .andReturn();
        Long goalIdA = objectMapper.readTree(gRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateGoalTaskRequest tReq = new CreateGoalTaskRequest();
        tReq.setTitle("用户A私密子任务");
        MvcResult tRes = mockMvc.perform(post("/api/goals/" + goalIdA + "/tasks")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tReq)))
                .andExpect(status().isOk())
                .andReturn();
        Long taskIdA = objectMapper.readTree(tRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 1. 用户 B 尝试获取用户 A 的目标 -> 404
        mockMvc.perform(get("/api/goals/" + goalIdA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 2. 用户 B 尝试修改用户 A 的目标 -> 404
        UpdateGoalRequest uReq = new UpdateGoalRequest();
        uReq.setTitle("恶意篡改目标");
        mockMvc.perform(put("/api/goals/" + goalIdA)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 3. 用户 B 尝试向用户 A 的目标注入子任务 -> 404
        CreateGoalTaskRequest injectReq = new CreateGoalTaskRequest();
        injectReq.setTitle("恶意注入子任务");
        mockMvc.perform(post("/api/goals/" + goalIdA + "/tasks")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(injectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 4. 用户 B 尝试修改用户 A 的子任务状态 -> 404
        mockMvc.perform(patch("/api/goals/" + goalIdA + "/tasks/" + taskIdA + "/status")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateGoalTaskStatusRequest("DONE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 5. 用户 B 尝试删除用户 A 的子任务 -> 404
        mockMvc.perform(delete("/api/goals/" + goalIdA + "/tasks/" + taskIdA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 6. 用户 B 尝试删除用户 A 的目标 -> 404
        mockMvc.perform(delete("/api/goals/" + goalIdA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 7. 用户 B 列表查询为 0
        mockMvc.perform(get("/api/goals")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records", hasSize(0)));
    }
}

