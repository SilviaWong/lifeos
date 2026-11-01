# =================================================================
# LifeOS Backend Multi-Stage Production Dockerfile
# =================================================================

# Stage 1: 构建阶段
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 先复制 POM 依赖定义以利用 Docker 缓存层
COPY pom.xml .
COPY personal-common/pom.xml personal-common/
COPY personal-system/pom.xml personal-system/
COPY personal-business/pom.xml personal-business/
COPY personal-statistics/pom.xml personal-statistics/
COPY personal-admin/pom.xml personal-admin/

# 复制各模块源代码
COPY personal-common/src personal-common/src
COPY personal-system/src personal-system/src
COPY personal-business/src personal-business/src
COPY personal-statistics/src personal-statistics/src
COPY personal-admin/src personal-admin/src

# 执行构建并跳过单元测试（测试在 CI 流水线前置执行）
RUN mvn clean package -DskipTests -B

# Stage 2: 运行时精简镜像
FROM eclipse-temurin:21-jre-alpine AS runner

LABEL maintainer="LifeOS Team"
LABEL description="LifeOS Backend Application Production Image"

# 创建专用的非 root 运行账号
RUN addgroup -S lifeos && adduser -S lifeos -G lifeos

WORKDIR /app

# 安装必要的工具包（curl 用于容器健康检查，tzdata 用于时区统一）
RUN apk add --no-cache curl tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# 从构建镜像中复制打包生成的单一可执行 Fat JAR
COPY --from=builder /build/personal-admin/target/personal-admin-1.0.0-SNAPSHOT.jar /app/app.jar

# 创建日志目录并授权
RUN mkdir -p /var/log/lifeos && chown -R lifeos:lifeos /app /var/log/lifeos

USER lifeos

EXPOSE 8080

# 容器探活健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/health-check || exit 1

# 生产环境 JVM 调优参数与容器内存自动感知
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]

