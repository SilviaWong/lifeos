package com.lifeos.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("认证中心端点测试 (注册/登录/退出/Token 鉴权)")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("测试用户注册 - 成功与重复用户名拒绝")
    void testRegisterFlow() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("test_user_01");
        dto.setPassword("password123");
        dto.setNickname("测试人员01");
        dto.setPhone("13800138000");

        // 1. 首次注册成功
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.username", is("test_user_01")))
                .andExpect(jsonPath("$.data.roles", hasItem("USER")))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        // 2. 重复注册相同用户名被拒绝 (400 / 资源已存在)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", not(0)))
                .andExpect(jsonPath("$.message", containsString("用户名已存在")));
    }

    @Test
    @DisplayName("测试用户登录 - 正确密码成功，错误密码被拒绝")
    void testLoginFlow() throws Exception {
        // 先注册
        RegisterDTO reg = new RegisterDTO();
        reg.setUsername("login_tester");
        reg.setPassword("correct_pwd");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        // 错误密码登录
        LoginDTO wrongLogin = new LoginDTO("login_tester", "wrong_pwd");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", is("用户名或密码错误")));

        // 正确密码登录
        LoginDTO correctLogin = new LoginDTO("login_tester", "correct_pwd");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.userInfo.username", is("login_tester")));
    }

    @Test
    @DisplayName("测试受保护接口 /api/auth/me - 无 Token 返回 401，有效 Token 返回当前用户信息")
    void testProtectedMeEndpoint() throws Exception {
        // 1. 无 Token 请求受保护接口，预期返回 HTTP 401
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(401)))
                .andExpect(jsonPath("$.message", containsString("请先登录")));

        // 2. 注册并登录获取 Token
        RegisterDTO reg = new RegisterDTO();
        reg.setUsername("me_tester");
        reg.setPassword("secret123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDTO("me_tester", "secret123"))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("token").asText();

        // 3. 携带 Token 请求 /api/auth/me，预期返回 200 成功
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.username", is("me_tester")));
    }
}

