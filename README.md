# LifeOS 个人生活管理全能操作系统

<p align="center">
  <b>融合待办、目标、健康、番茄钟、习惯、记账、备忘日记与运营大屏的下一代个人生活操作系统</b>
</p>

---

## 一、项目愿景与核心功能

**LifeOS** 是一款专为追求高效自律、身心健康与清晰财务规划的用户打造的一体化生活管理平台。系统通过严格的用户数据隔离、自动化进度度量与智能提醒调度，将碎片化的个人生活记录串联为有机的目标达成闭环。

### 核心功能模块一览

- 📋 **待办管理 (Todo)**：支持标题、描述、优先级（高/中/低）、计划开始与截止时间、状态流转（待办/进行中/已完成）及多维筛选。
- 🎯 **目标与子任务 (Goal & Tasks)**：拆解长期愿景为可执行子任务，服务端根据完成比例动态计算目标进度百分比。
- 🏃 **健康记录与趋势 (Health)**：记录步数、睡眠时长、体重、饮水、饮食卡路里等指标，并提供区间健康均值与 ECharts 趋势图表。
- ⏱️ **专注番茄钟 (Pomodoro)**：专注时长/休息时长自定义配置，服务端记录有效专注会话，统计每日专注时长与完成频次。
- 📅 **习惯养成打卡 (Habit)**：支持每日/每周/每月打卡周期，数据库唯一约束保证同日幂等打卡，自动计算连续打卡天数与月度日历热力图。
- 💰 **收支管理与统计 (Finance)**：高精度 `BigDecimal` 记账体系，支持支出/收入明细、多级分类标签、月度收支总额与分类占比分析。
- 📦 **生活记录 (Life Records)**：个人闲置物品归档与借还历史追踪、多标签置顶备忘录、私密心情日记（敏感内容日志脱敏）。
- 🔔 **提醒中心 (Reminder)**：基于规则引擎扫描待办截止、习惯打卡、目标到期事件，支持防重幂等触发与用户站内提醒通知。
- 📊 **管理端综合大屏 (Admin Dashboard)**：面向平台运营的宏观数据看板，包含活跃用户趋势、各业务模块使用频次、操作审计日志。

---

## 二、系统架构与技术选型

### 1. 技术栈

| 层次 | 核心技术选型 | 说明 |
| :--- | :--- | :--- |
| **后端框架** | Spring Boot 3.2.5, JDK 21 | 现代化微内核架构与虚拟线程支持 |
| **持久层** | MyBatis-Plus 3.5.5, HikariCP | 自动分页、代码生成与高并发连接池 |
| **安全与认证** | Spring Security 6, JJWT 0.12.5 | 无状态 Bearer Token，RBAC 角色权限模型 |
| **接口文档** | Springdoc OpenAPI 3, Swagger UI | 规范化 RESTful API 契约与在线交互调试 |
| **数据存储** | MySQL 8.0.36 | InnoDB 引擎，utf8mb4 字符集 |
| **用户端应用** | uni-app (Vue 3 + TS + Vite + Pinia) | 一套代码发布至 H5 移动网页与微信小程序 |
| **管理端后台** | Vue 3 + TypeScript + Element Plus + ECharts 5 | 桌面端高交互响应式管理工作台 |
| **接入与网关** | Nginx 1.27, Docker, Docker Compose | 反向代理、Gzip 压缩、SSL 卸载与容器编排 |

### 2. 代码仓库结构

```text
lifeos/
├── backend/                        # 后端多模块工程
│   ├── personal-common/            # 统一响应封装、错误码、全局异常拦截、工具类
│   ├── personal-system/            # 用户身份认证、RBAC 角色、JWT 拦截器
│   ├── personal-business/          # 待办/目标/健康/番茄钟/习惯/收支/生活记录/提醒服务
│   ├── personal-statistics/        # 首页摘要、用户指标统计、管理端看板
│   ├── personal-admin/             # 启动主程序、统一资源配置、全量测试套件
│   └── Dockerfile                  # 后端多阶段构建 Dockerfile
├── user-app/                       # 用户端移动应用 (uni-app Vue 3)
│   ├── src/pages/                  # 待办、目标、健康、习惯、记账、番茄钟等页面
│   ├── src/api/                    # 封装 RESTful 请求模块
│   └── src/stores/                 # Pinia 状态管理
├── admin-web/                      # 管理端后台应用 (Vue 3 + Element Plus)
│   ├── src/views/                  # 数据大屏、用户管理、审计日志等管理页面
│   └── src/components/             # 可复用业务与图表组件
├── docker/                         # 容器化部署配置
│   └── nginx/                      # Nginx 反向代理配置与前端镜像构建
├── docs/                           # 项目需求与设计规范
│   ├── LifeOS_PRD.md               # 业务需求规格说明书
│   ├── LifeOS_ER设计.md             # 数据库 ER 图与实体设计
│   ├── lifeos_schema_mysql8.sql    # MySQL 8.0 全量 DDL 与初始字典数据
│   └── DEPLOYMENT_GUIDE.md         # 生产环境部署与运维手册
├── docker-compose.yml              # 生产容器化一键部署编排
├── .env.example                    # 本地开发环境变量模板
└── .env.prod.example               # 生产部署环境变量模板
```

---

## 三、快速开始（本地开发）

### 1. 准备工作
- 安装 **JDK 21**、**Maven 3.8+**、**Node.js 20+** 与 **pnpm 9+**。
- 本地安装并启动 **MySQL 8.0**。

### 2. 初始化数据库
连接本地 MySQL 服务并执行初始化脚本：
```bash
# 1. 初始化表结构与系统字典
mysql -u root -p < docs/lifeos_schema_mysql8.sql

# 2. 可选：导入预置演示与测试数据 (含 admin 与 demo 完整初始数据)
mysql -u root -p < docs/lifeos_seed_data.sql
```
> **提示**：若使用 `docker-compose.dev.yml`，启动时会自动执行上述两个脚本，无需手动导入。

### 3. 预置演示与测试账号

| 账号身份 | 用户名 | 密码 | 对应角色 | 推荐体验端 | 数据内容说明 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **平台管理员** | `admin` | `admin123` | `ROLE_ADMIN` | 管理端 (`admin-web`) | 综合大屏看板、运营审计日志、全平台用户管理 |
| **普通用户** | `demo` | `demo123` | `ROLE_USER` | 用户端 (`user-app`) | 预置完整待办、目标愿景、健康记录、习惯打卡、收支记账、物品借还、备忘录与日记 |

### 3. 配置本地环境变量
复制环境变量模板：
```bash
cp .env.example .env
```
根据本地实际情况修改 `.env` 中的 `DB_USER` 与 `DB_PASSWORD`。

### 4. 启动后端应用
```bash
mvn -f backend/pom.xml spring-boot:run
```
后端服务默认监听在 `http://localhost:8080`。  
- Swagger API 文档地址：`http://localhost:8080/swagger-ui.html`
- 服务探活健康检查：`http://localhost:8080/api/health-check`

### 5. 启动用户端前端 (user-app)
```bash
cd user-app
pnpm install
# 启动 H5 开发模式
pnpm run dev:h5
```
访问本地开发页面：`http://localhost:5173`。

### 6. 启动管理端前端 (admin-web)
```bash
cd admin-web
pnpm install
pnpm run dev
```
访问管理工作台：`http://localhost:3000`。

---

## 四、自动化测试与代码质量

项目中配备了 64 个自动化单元测试与端到端集成测试，覆盖水平越权防御、垂直 RBAC 隔离、分页上限截断、防重复打卡及日记隐私脱敏。

```bash
# 执行后端全量测试套件
mvn -f backend/pom.xml test

# 执行前端 TypeScript 严谨类型检查
cd user-app && npx tsc --noEmit
cd ../admin-web && npx vue-tsc --noEmit

# 执行全端生产打包验证
cd user-app && pnpm run build:h5 && pnpm run build:mp-weixin
cd ../admin-web && pnpm run build
```

---

## 五、生产环境部署

系统支持 **Docker Compose 一键容器化部署** 与 **传统 Linux 主机 Systemd 部署**。详细指南请查阅 [生产环境部署手册 (docs/DEPLOYMENT_GUIDE.md)](file:///Users/wangyanan/Projects2/lifeos/docs/DEPLOYMENT_GUIDE.md)。

### Docker Compose 一键启动命令：
```bash
# 1. 复制生产环境变量
cp .env.prod.example .env
# 2. 编译前端产物
(cd user-app && pnpm run build:h5) && (cd admin-web && pnpm run build)
# 3. 启动全套服务（包含 MySQL 8.0、后端集群、Nginx 网关）
docker compose up -d --build
```

---

## 六、安全与合规声明

1. **凭证隔离**：本项目 Git 仓库严格执行凭据过滤，不包含任何生产环境账号、明文密码、真实 JWT 密钥或数据库凭据。
2. **生产初始化**：管理员账号初始化采用标准的 Spring Security BCrypt 强哈希算法存储，严禁在数据库脚本中硬编码弱口令。
3. **数据安全**：所有用户级数据操作均由服务端 JWT 上下文提取用户身份，严格杜绝水平或垂直越权漏洞。

---

## 七、开源与许可

LifeOS 遵循商业软件严谨设计标准开发，交付物归属于团队及项目所有者。

