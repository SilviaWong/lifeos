# LifeOS 后端服务 (LifeOS Backend)

基于 Spring Boot 3.2.x + Java 17 + MyBatis-Plus + MySQL 8.0 构建的模块化单体架构后端。

## 1. 模块结构

- `personal-common`：全局统一响应包装 `ApiResponse<T>`、分页基类 `PageQuery`/`PageResult<T>`、通用错误码与业务异常处理、Jackson 序列化配置、MyBatis-Plus 插件、链路追踪 `X-Trace-Id` 拦截器。
- `personal-system`：用户、角色、认证上下文、Security 基础配置（阶段 2 核心）。
- `personal-business`：健康指标、目标管理、日程待办、番茄钟、习惯打卡、收支记账、生活记录、提醒规则。
- `personal-statistics`：用户首页数据看板聚合、管理端运营与模块使用统计。
- `personal-admin`：Spring Boot 主启动入口、环境配置装配、监控探活与集成测试。

## 2. 运行环境要求

- JDK 17 或以上（支持 LTS 17 / LTS 21）
- Maven 3.8+
- MySQL 8.0+（字符集 `utf8mb4`，排序规则 `utf8mb4_0900_ai_ci`）

## 3. 本地快速启动

### 方式一：使用测试配置启动（无需外部 MySQL）
```bash
cd backend
mvn spring-boot:run -pl personal-admin -Dspring-boot.run.profiles=test
```

### 方式二：连接本地开发 MySQL 启动
1. 复制 `.env.example` 为 `.env` 并填写本地 MySQL 连接信息：
```bash
cp .env.example .env
```
2. 启动服务（默认 dev 环境）：
```bash
mvn clean spring-boot:run -pl personal-admin
```

## 4. 核心端点与验证

- 服务健康检查：[http://localhost:8080/api/health-check](http://localhost:8080/api/health-check)
- Actuator 监控：[http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- Swagger 接口文档：[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI 规范定义：[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## 5. 测试与编译

```bash
# 执行完整单元测试与集成测试
mvn clean test
```

