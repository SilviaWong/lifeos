package com.lifeos.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Spring Boot 上下文启动验证测试")
class LifeOsApplicationTests {

    @Test
    @DisplayName("验证 Spring 上下文各模块 Bean 正常加载与装配")
    void contextLoads() {
        // 若各模块配置无冲突、Bean 依赖无环，此处能正常加载上下文
    }
}

