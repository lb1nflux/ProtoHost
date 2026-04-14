# ProtoHost

A prototype hosting platform. Upload ZIP files containing HTML prototypes and share them via public or password-protected links.

## Tech Stack

- **Backend**: Spring Boot 3.2.5 + MyBatis-Plus + MySQL 8
- **Frontend**: Vue 3 + Vite + Tailwind CSS
- **Auth**: JWT

## Requirements

- Java 17+
- Node.js 18+
- MySQL 8.x
- Maven 3.x

## Setup

### 1. Database

```sql
CREATE DATABASE protohost CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Then run `backend/src/main/resources/schema.sql` to create tables.

### 2. Backend Configuration

```bash
cp backend/src/main/resources/application.yml.example \
   backend/src/main/resources/application.yml
```

Edit `application.yml` and fill in:

| Key | Description |
|-----|-------------|
| `spring.datasource.username` | MySQL username |
| `spring.datasource.password` | MySQL password |
| `app.jwt.secret` | Base64 secret key — generate with `openssl rand -base64 32` |
| `app.upload.base-path` | Directory for uploaded files (default: `./uploads`) |

### 3. Start Backend

```bash
cd backend
mvn spring-boot:run
```

### 4. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173`, proxies API calls to `http://localhost:8080`.

## Features

- Register / Login with JWT authentication
- Upload ZIP prototypes (UTF-8 and GBK filename support)
- Public or password-protected sharing via slug URL
- Prototype preview in iframe with asset proxying
