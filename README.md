# ProtoHost

原型托管平台。上传包含 HTML 原型的 ZIP 文件，通过公开或密码保护的链接分享。

## 技术栈

- **后端**：Spring Boot 3.2.5 + MyBatis-Plus + MySQL 8
- **前端**：Vue 3 + Vite + Tailwind CSS
- **认证**：JWT

## 环境要求

- Docker & Docker Compose

## Docker 部署

```bash
cp .env.example .env
# 编辑 .env，填写 MYSQL_ROOT_PASSWORD 和 JWT_SECRET
docker compose up -d
```

服务启动后访问 `http://localhost`。

## 手动开发环境

### 1. 数据库

```sql
CREATE DATABASE protohost CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后执行 `backend/src/main/resources/schema.sql` 创建表结构。

### 2. 后端配置

```bash
cp backend/src/main/resources/application.yml.example \
   backend/src/main/resources/application.yml
```

编辑 `application.yml`，填写以下配置：

| 配置项 | 说明 |
|--------|------|
| `spring.datasource.username` | MySQL 用户名 |
| `spring.datasource.password` | MySQL 密码 |
| `app.jwt.secret` | Base64 密钥，可用 `openssl rand -base64 32` 生成 |
| `app.upload.base-path` | 上传文件目录（默认：`./uploads`） |

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，API 请求代理到 `http://localhost:8080`。

## 功能

- 注册 / 登录（JWT 认证）
- 上传 ZIP 原型（支持 UTF-8 和 GBK 文件名）
- 通过 slug URL 公开或密码保护分享
- iframe 预览原型，支持资源代理
