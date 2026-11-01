# LifeOS 生产环境部署与运维手册

本文档提供 LifeOS 系统在生产环境下的完整部署方案、环境配置、服务启停、数据备份恢复及版本更新回滚操作规范。

---

## 一、系统架构与端口拓扑

```
[ 客户端浏览器 / 移动端 H5 / 小程序 ]
                 │
                 ▼
      [ Nginx 反向代理网关 (80 / 443) ]
       ├── /admin/   ──> 管理端 Web 静态资源 (Vue 3 + Element Plus)
       ├── /         ──> 用户端 H5 静态资源 (uni-app)
       └── /api/     ──> 后端服务集群 (Spring Boot 3, 内部端口 8080)
                             │
                             ▼
                [ MySQL 8.0 关系型数据库 (3306) ]
```

- **对外开放端口**：仅开放 `80` (HTTP) 与 `443` (HTTPS)。
- **内部安全隔离**：后端应用（`8080`）与 MySQL（`3306`）仅监听内网或运行在 Docker 隔离网络中，禁止直接暴露公网访问。

---

## 二、环境与依赖版本要求

| 组件 | 推荐生产版本 | 最低版本要求 | 用途说明 |
| :--- | :--- | :--- | :--- |
| **操作系统** | Ubuntu 22.04 LTS / Debian 12 / AlmaLinux 9 | 64 位 Linux | 生产宿主系统 |
| **JDK** | Eclipse Temurin 21.0.2+ | OpenJDK 21 | 后端运行时 |
| **MySQL** | MySQL 8.0.36+ | MySQL 8.0 | 业务数据持久化 |
| **Nginx** | Nginx 1.26+ | Nginx 1.20 | 反向代理与静态资源托管 |
| **Node.js** | Node.js 20.x LTS | Node.js 18.x | 前端应用构建（CI环境） |
| **pnpm** | pnpm 9.x | pnpm 8.x | 前端依赖包管理 |
| **Docker** (可选) | Docker 25.x+ & Compose v2 | Docker 20.10 | 容器化一键编排部署 |

---

## 三、数据库初始化与安全迁移

### 1. 数据库全新安装初始化
使用管理员权限登录 MySQL 8.0，执行全量建表及初始字典数据脚本：

```bash
# 1. 登录 MySQL 命令行
mysql -u root -p

# 2. 执行建表脚本（脚本包含创建库 lifeos、全部 22 张表结构、系统基础角色及收支分类）
source /path/to/lifeos/docs/lifeos_schema_mysql8.sql;
```

### 2. 生产数据库升级与平滑迁移规范
> [!CAUTION] 
> 严禁在生产环境执行 `DROP TABLE`、`TRUNCATE` 或任何破坏已有数据的 SQL 操作！

数据库增量升级必须遵守以下原则：
1. **预备份**：升级前必须全量备份当前数据库。
2. **只增不改**：新增字段使用 `ALTER TABLE ... ADD COLUMN ... DEFAULT NULL`，避免因锁表造成生产事务阻塞。
3. **索引并发创建**：新建大表索引时评估数据量大小，选择业务低峰期执行。
4. **可回滚**：每个增量升级脚本必须同步准备对应的降级回滚脚本（Down Migration）。

---

## 四、部署方案 A：Docker Compose 容器化一键部署（推荐）

### 1. 准备配置文件
在部署目录复制生产环境配置模板：
```bash
cp .env.prod.example .env
```
使用文本编辑器（如 `vim .env`）修改生产安全凭据：
```bash
# 必须设置高强度随机密码和至少 256 位的 JWT Secret
DB_PASSWORD=SecurePassword_ChangeMe_2026!
MYSQL_ROOT_PASSWORD=RootPassword_ChangeMe_2026!
JWT_SECRET=$(openssl rand -base64 32)
```

### 2. 构建前端静态资源
在 CI 流水线或部署构建机上打包前端产物：
```bash
# 构建用户端 H5
cd user-app
pnpm install --frozen-lockfile
pnpm run build:h5
cd ..

# 构建管理端 Web
cd admin-web
pnpm install --frozen-lockfile
pnpm run build
cd ..
```

### 3. 启动容器集群
```bash
# 构建镜像并后台启动服务
docker compose up -d --build

# 检查各容器健康状态
docker compose ps
```

预期看到 `lifeos-mysql` (healthy)、`lifeos-backend` (healthy) 与 `lifeos-nginx` (running)。

---

## 五、部署方案 B：传统主机裸机部署 (Systemd)

### 1. 后端打包与服务托管
#### 编译生成可执行 Fat JAR：
```bash
mvn -f backend/pom.xml clean package -DskipTests
# 生成产物：backend/personal-admin/target/personal-admin-1.0.0-SNAPSHOT.jar
```

#### 配置 Systemd 服务单元文件 `/etc/systemd/system/lifeos.service`：
```ini
[Unit]
Description=LifeOS Backend Service
After=syslog.target network.target mysql.service

[Service]
Type=simple
User=lifeos
Group=lifeos
EnvironmentFile=/etc/lifeos/lifeos.env
ExecStart=/usr/bin/java $JAVA_OPTS -jar /opt/lifeos/personal-admin-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```

#### 启停与日志管理：
```bash
# 重新加载服务
sudo systemctl daemon-reload
# 启动服务
sudo systemctl start lifeos
# 设置开机自启
sudo systemctl enable lifeos
# 查看运行状态
sudo systemctl status lifeos
# 查看实时运行日志
sudo journalctl -u lifeos -f
```

---

## 六、HTTPS 证书与安全加固配置 (Nginx)

生产上线推荐开启 HTTPS 加密传输。使用 Certbot 申请免费 Let's Encrypt 证书或挂载企业 SSL 证书：

```nginx
server {
    listen 80;
    server_name lifeos.yourdomain.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name lifeos.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/lifeos.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/lifeos.yourdomain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 1d;

    # HSTS 安全加固（强制浏览器 1 年内均走 HTTPS）
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    # 包含现有反向代理与静态资源托管规则
    include /etc/nginx/conf.d/default.conf;
}
```

---

## 七、运维日常：备份、恢复与故障排查

### 1. 数据库热备份脚本 (Crontab 定时任务)
编写备份脚本 `/opt/scripts/backup_mysql.sh`：
```bash
#!/bin/bash
BACKUP_DIR="/data/backup/mysql"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
FILENAME="$BACKUP_DIR/lifeos_db_$TIMESTAMP.sql.gz"

mkdir -p $BACKUP_DIR

# 使用 mysqldump 进行一致性热备份（--single-transaction）
mysqldump -h 127.0.0.1 -u root -p"$MYSQL_ROOT_PASSWORD" \
  --default-character-set=utf8mb4 \
  --single-transaction \
  --quick \
  --databases lifeos | gzip > "$FILENAME"

# 仅保留最近 30 天的备份文件
find $BACKUP_DIR -name "lifeos_db_*.sql.gz" -mtime +30 -exec rm -f {} \;
echo "[$TIMESTAMP] Database backup finished: $FILENAME"
```
加入定时任务每天凌晨 2 点执行：
```cron
0 2 * * * /bin/bash /opt/scripts/backup_mysql.sh >> /var/log/backup_mysql.log 2>&1
```

### 2. 灾难数据恢复流程
```bash
# 1. 确认恢复文件并解压
gunzip < /data/backup/mysql/lifeos_db_20261101_020000.sql.gz > /tmp/restore.sql

# 2. 导入数据恢复（执行前请停止后端写入）
mysql -h 127.0.0.1 -u root -p lifeos < /tmp/restore.sql

# 3. 清理临时解压文件
rm -f /tmp/restore.sql
```

---

## 八、版本滚动更新与紧急回滚

### 1. 标准平滑升级流程
1. **构建阶段**：打包新的后端 Jar 包与前端构建产物。
2. **备份当前运行版本**：
   ```bash
   cp /opt/lifeos/personal-admin-1.0.0-SNAPSHOT.jar /opt/lifeos/backup/personal-admin-last-known-good.jar
   ```
3. **数据库迁移**：执行版本配套的增量 SQL 脚本。
4. **替换与重启**：
   ```bash
   sudo systemctl stop lifeos
   cp target/personal-admin-1.0.0-SNAPSHOT.jar /opt/lifeos/
   sudo systemctl start lifeos
   ```
5. **部署验证**：
   ```bash
   curl -i http://localhost:8080/api/health-check
   ```

### 2. 紧急回滚方案 (Rollback)
当新版本发布出现严重线上故障时：
1. **停止故障服务**：`sudo systemctl stop lifeos`。
2. **回滚后端应用**：
   ```bash
   cp /opt/lifeos/backup/personal-admin-last-known-good.jar /opt/lifeos/personal-admin-1.0.0-SNAPSHOT.jar
   ```
3. **回滚前端资源**：恢复上一版本的 `dist/` 静态文件夹。
4. **数据库降级**：执行当前版本的 Down Migration 回滚 SQL 脚本。
5. **重启验证**：`sudo systemctl start lifeos && curl -i http://localhost:8080/api/health-check`。

