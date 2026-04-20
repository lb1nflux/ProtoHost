# ProtoHost

ProtoHost is a full-stack application for hosting and sharing HTML prototype packages uploaded as ZIP files.

## Stack

- Backend: Spring Boot 3, Spring Security, MyBatis-Plus, MySQL 8
- Frontend: Vue 3, Vite, Pinia, Tailwind CSS
- Deployment: Docker Compose

## Repository Scope

This repository keeps only the assets needed for development, build, deployment, and maintenance.
Product drafts, design-process materials, local agent metadata, and secrets are excluded from the public repo.

## Quick Start

### Docker Compose

1. Copy `.env.example` to `.env`.
2. Replace `MYSQL_ROOT_PASSWORD` and `JWT_SECRET`.
3. Configure mail variables if you need email verification or password reset.
4. Run:

```bash
docker compose up --build
```

The frontend will be available at `http://localhost`.

### Local Development

1. Create the database:

```sql
CREATE DATABASE protohost CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Import [`backend/src/main/resources/schema.sql`](/C:/Users/79628/Desktop/protohost-github/backend/src/main/resources/schema.sql).
3. Copy [`backend/src/main/resources/application.yml.example`](/C:/Users/79628/Desktop/protohost-github/backend/src/main/resources/application.yml.example) to `backend/src/main/resources/application.yml` and fill in local values.
4. Start the backend:

```bash
cd backend
mvn spring-boot:run
```

5. Start the frontend:

```bash
cd frontend
npm install
npm run dev
```

The frontend dev server runs on `http://localhost:5173`.

## Core Features

- User registration and login
- Email verification and password reset
- ZIP upload and hosted preview for HTML prototypes
- Public and password-protected share links
- Project grouping, versioning, and downloads

## Push Checklist

- Do not commit `.env` or `backend/src/main/resources/application.yml`.
- Keep mail credentials and JWT secrets in local-only config or CI/CD secrets.
- Do not restore `context/`, `.gemini/`, `GEMINI.md`, or similar workspace-only materials into the public repository.
