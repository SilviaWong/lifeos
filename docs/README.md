# LifeOS 交付包

- `LifeOS_PRD_V1.0.docx`：可阅读/评审的 PRD 文档
- `LifeOS_PRD.md`：Markdown 版 PRD，便于 Git 管理和 AI 编码工具读取
- `LifeOS_ER设计.md`：实体关系与 ER 图（Mermaid）
- `lifeos_schema_mysql8.sql`：MySQL 8.0 初始化 DDL 和基础字典数据

## 执行 SQL
```bash
mysql -u root -p < lifeos_schema_mysql8.sql
```

请在开发库执行。生产环境部署前，应先评审账号初始化、备份策略、时区策略及健康数据隐私要求。

## 重要实现约束
- 普通用户业务数据必须由后端根据 JWT 身份取得 `user_id`，不能信任请求体传入的 user_id。
- 本 SQL 有意不建立大量物理外键，关系由 Service 层校验；上线前可按团队规范补充外键。
- 管理员账号请通过应用初始化程序写入 BCrypt 密码哈希，不要在 SQL 中写明文密码。
