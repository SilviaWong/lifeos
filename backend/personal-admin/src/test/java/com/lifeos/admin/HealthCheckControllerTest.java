package com.lifeos.admin;

import com.lifeos.system.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("健康检查与基础接口测试")
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("验证 /api/health-check 接口返回 200 且符合统一 ApiResponse 契约")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/health-check")
                        .header("X-Trace-Id", "manual-test-trace-id")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", "manual-test-trace-id"))
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.message", is("服务运行正常")))
                .andExpect(jsonPath("$.data.status", is("UP")))
                .andExpect(jsonPath("$.data.application", is("lifeos-backend")))
                .andExpect(jsonPath("$.traceId", is("manual-test-trace-id")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("验证已认证请求访问不存在的路径时返回全局 404 ApiResponse")
    void testNotFoundRoute() throws Exception {
        String token = jwtTokenProvider.generateToken(999L, "mock_user", List.of("USER"));

        mockMvc.perform(get("/api/non-existent-route")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(404)))
                .andExpect(jsonPath("$.message", containsString("请求的资源或路径不存在")));
    }
}
