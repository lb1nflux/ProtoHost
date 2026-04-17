# ProtoHost Project Context

ProtoHost is a web application designed for hosting and sharing HTML prototypes. Users can upload ZIP files, which are then accessible via shareable slugs, with support for public access or password protection.

## Project Overview

- **Architecture**: Monorepo with a Java Spring Boot backend and a Vue.js frontend.
- **Backend**:
  - **Framework**: Spring Boot 3.2.5
  - **ORM**: MyBatis-Plus
  - **Database**: MySQL 8.0
  - **Authentication**: JWT (JSON Web Token)
  - **Language**: Java 17
- **Frontend**:
  - **Framework**: Vue 3 (Composition API)
  - **Build Tool**: Vite
  - **Styling**: Tailwind CSS
  - **State Management**: Pinia
  - **HTTP Client**: Axios
  - **Icons**: Lucide Vue Next
- **Deployment**: Docker & Docker Compose

## Directory Structure

- `backend/`: Java source code, Maven configuration, and database migrations.
- `frontend/`: Vue source code, Vite configuration, and static assets.
- `docker-compose.yml`: Full stack orchestration.
- `.env.example`: Template for environment variables.

## Building and Running

### Full Stack (Docker)
1. Copy environment variables: `cp .env.example .env`
2. Configure `.env` (set `MYSQL_ROOT_PASSWORD` and `JWT_SECRET`).
3. Start services: `docker compose up -d`
4. Access at `http://localhost`.

### Backend Development
1. Navigate to directory: `cd backend`
2. Prepare database: Create `protohost` database and run `src/main/resources/schema.sql`.
3. Configure `application.yml`: `cp src/main/resources/application.yml.example src/main/resources/application.yml` and fill in details.
4. Run application: `mvn spring-boot:run`
5. API endpoint: `http://localhost:8080`

### Frontend Development
1. Navigate to directory: `cd frontend`
2. Install dependencies: `npm install`
3. Start development server: `npm run dev`
4. Access at `http://localhost:5173` (proxies `/api` to `localhost:8080`).

## Development Conventions

- **API Design**: All backend APIs are prefixed with `/api`.
- **Authentication**: 
  - Uses `Authorization: Bearer <token>` header.
  - JwtFilter handles token validation.
- **Frontend Practices**:
  - Alias `@` refers to `frontend/src`.
  - Component-based architecture in `frontend/src/components`.
  - Type-safe development with TypeScript.
- **Database**:
  - Primary keys use `BIGINT AUTO_INCREMENT`.
  - Timestamps for `created_at` and `updated_at` (automatic via MySQL).
- **File Storage**:
  - Prototypes are unzipped and stored in the directory specified by `app.upload.base-path` (default `./uploads`).
  - GBK and UTF-8 encoding support for ZIP files is implemented in `ZipUtil.java`.

## Key Files

- `backend/src/main/resources/schema.sql`: Database schema definition.
- `backend/src/main/java/com/protohost/config/SecurityConfig.java`: Spring Security and JWT configuration.
- `frontend/vite.config.ts`: Vite setup including the API proxy.
- `frontend/src/router/index.ts`: Frontend routing logic.
- `frontend/src/stores/auth.ts`: Authentication state management.
