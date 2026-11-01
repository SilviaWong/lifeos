# =================================================================
# LifeOS 前端与 Nginx 网关容器镜像构建文件
# =================================================================

# 生产运行时 Nginx 镜像
FROM nginx:1.27-alpine

LABEL maintainer="LifeOS Team"
LABEL description="LifeOS Frontend Nginx Gateway Production Image"

# 安装时区支持与 curl 健康检查工具
RUN apk add --no-cache curl tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# 移除 Nginx 默认配置
RUN rm -rf /etc/nginx/conf.d/default.conf /usr/share/nginx/html/*

# 复制自定义 Nginx 配置文件
COPY docker/nginx/default.conf /etc/nginx/conf.d/default.conf

# 复制前端编译构建产物 (构建产物由 CI 流水线或本地 pnpm build 产出)
COPY admin-web/dist /usr/share/nginx/html/admin
COPY user-app/dist/build/h5 /usr/share/nginx/html/user-app

EXPOSE 80

# Nginx 容器探活检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]

