# COW SYSTEM 部署与联调指南

本仓库包含两个工程：
- `backend/`：Spring Boot + Maven
- `frontend/`：Vite + React + TypeScript + Ant Design

已提供根目录统一编排：`docker-compose.yml`。

## 一、本地一键启动

### 1. 前置条件
- 已安装 Docker
- 已安装 Docker Compose（Docker Desktop 内置即可）

### 2. 启动命令

```bash
docker compose up -d --build
```

### 3. 验证方式
- 浏览器访问：`http://localhost`
- 使用账号登录：`admin / admin`

### 4. 服务说明
- 对外仅开放：`80`（Nginx）
- 内部服务：
  - MySQL：`mysql:3306`
  - Redis：`redis:6379`
  - Backend：`backend:8080`
- MySQL、Redis 已启用 volume 持久化。

## 二、阿里云 ECS 部署指南

### 1. ECS 建议
- 系统：Alibaba Cloud Linux 3 / Ubuntu 22.04+
- 规格：至少 2 vCPU / 4GB 内存（建议）

### 2. 安装 Docker 与 Compose

Ubuntu 示例：

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo $VERSION_CODENAME) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable --now docker
```

### 3. 安全组配置
- 必须开放：`80`
- 可选开放：`443`
- 不建议开放：`3306`、`6379`

### 4. 上传并启动

```bash
# 上传代码到 ECS（git clone / scp 均可）
cd COW\ SYSTEM

docker compose up -d --build
```

## 三、数据库建议（生产）

生产环境建议使用阿里云 RDS MySQL，Redis 使用阿里云 Redis。

可通过环境变量覆盖后端连接配置（无需改业务代码）：
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATA_REDIS_HOST`
- `SPRING_DATA_REDIS_PORT`

`application-prod.yml` 示例（说明用途）：

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOST}
      port: ${SPRING_DATA_REDIS_PORT:6379}
```

## 四、域名与 HTTPS（可选）

### 方案 A：Nginx + certbot
- 域名解析到 ECS 公网 IP
- 在 ECS 申请 Let\'s Encrypt 证书并自动续期
- Nginx 增加 443 server 块并加载证书

### 方案 B：阿里云证书
- 在阿里云 SSL 证书服务申请/导入证书
- 下载 PEM 证书并挂载到 Nginx
- Nginx 配置 443 与强制 HTTPS 跳转

## 五、常见问题排查

### 1. 前端 Network Error
- 检查 `nginx`、`backend` 容器是否正常：
  ```bash
  docker compose ps
  docker compose logs -f nginx backend
  ```
- 确认访问入口是 `http://localhost`，而不是直接打后端容器地址。

### 2. 502 Bad Gateway
- 后端未启动或启动慢导致。
- 查看后端日志：
  ```bash
  docker compose logs -f backend
  ```

### 3. MySQL 连接失败
- 检查 MySQL 健康状态：
  ```bash
  docker compose ps
  docker compose logs -f mysql
  ```
- 确认数据库初始化完成后 backend 才会成功连接。

### 4. Redis 连接失败
- 查看 Redis 日志：
  ```bash
  docker compose logs -f redis
  ```
- 检查 backend 环境变量中的 `SPRING_DATA_REDIS_HOST=redis`。

### 5. 跨域 / 反代问题
- 生产建议统一走 Nginx 单入口（本方案已使用），前后端同域可避免跨域问题。
- 确认前端构建时 `VITE_API_BASE_URL=/api`。

### 6. 容器时区问题（Asia/Shanghai）
- 本编排已为主要服务设置 `TZ=Asia/Shanghai`。
- 若仍有时间偏差，检查宿主机时间同步（NTP）。

---

## 启动命令（最终）

```bash
docker compose up -d --build
```

## 六、阿里云拉取镜像超时解决方案

已将 Dockerfile 与 docker-compose.yml 中基础镜像统一切换为阿里云镜像源：`registry.cn-hangzhou.aliyuncs.com/library`。

验证命令：

```bash
docker pull registry.cn-hangzhou.aliyuncs.com/library/mysql:8.0
docker pull registry.cn-hangzhou.aliyuncs.com/library/nginx:1.27-alpine
```

启动命令：

```bash
docker-compose up -d --build
```
