# Library Management System

A full-stack library management system with role-based access, borrowing management, and a hybrid recommendation engine.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3 (Java 17) + Maven |
| Frontend | Vue 3 + Vite + TypeScript + Element Plus |
| Database | MySQL 8.x |
| Recommendation | Python (scikit-learn + numpy) |

## Roles

| Role | ID | Capabilities |
|------|----|-------------|
| **Admin** | 0 | User & book management, statistics, recommendation config, renewal approval |
| **Teacher** | 1 | Batch borrowing, recommendations, renewal request management |
| **Student** | 2 | Personalized recommendations, borrowing reminders, borrow history |

## Project Structure

```
├── backend/                     # Spring Boot backend
│   ├── src/main/java/com/example/library/
│   │   ├── config/              # Security & table initializers
│   │   ├── controller/          # REST controllers
│   │   ├── dto/                 # Request/response DTOs
│   │   ├── entity/              # JPA entities
│   │   ├── repository/          # Spring Data repositories
│   │   ├── service/             # Business logic
│   │   └── util/                # JWT, auth filter, role check
│   ├── src/main/resources/
│   │   └── application.yml      # App configuration
│   └── recommendation/          # Python hybrid recommender
├── frontend/                    # Vue 3 frontend
│   └── src/
│       ├── router/              # Vue Router config
│       ├── util/                # Auth utilities
│       └── views/               # Page components (admin/teacher/student)
└── library_management_system.sql # Database schema + seed data
```

## Prerequisites

- **JDK 17**
- **Maven 3.9+**
- **Node.js 18+**
- **MySQL 8.x**
- **Python 3.10+** (optional — only for the recommendation engine)

## Quick Start

### 1. Database

Create a MySQL database named `Library_Management_System` and import the provided SQL:

```bash
mysql -u root -p -e "CREATE DATABASE Library_Management_System;"
mysql -u root -p Library_Management_System < library_management_system.sql
```

### 2. Backend

Update `backend/src/main/resources/application.yml` with your MySQL credentials, then start:

```bash
cd backend
mvn spring-boot:run
```

The backend runs at `http://localhost:8080`. Verify with `GET /health` → `OK`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev -- --host 0.0.0.0 --port 5173
```

Visit `http://localhost:5173`. The Vite dev server proxies `/api/*` → `http://localhost:8080/*`.

### 4. Recommendation (optional)

```bash
pip install -r backend/recommendation/requirements.txt
```

## Configuration

Key settings in `backend/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/Library_Management_System?...
    username: root
    password: 123456

app:
  jwt:
    secret: replace-with-a-long-secret-key-at-least-32-bytes
  recommendation:
    python-command: python
    python-script: recommendation/hybrid_recommender.py
    default-top-n: 20
```

> ⚠️ Change the database credentials and JWT secret before deploying to production.

## Authentication

- Login endpoint: `POST /api/login`
- Returns a JWT token → include as `Authorization: Bearer <token>` in subsequent requests
- No built-in test accounts — add user records to `sys_user` table via the SQL dump or directly

## Routes

| Role | Routes |
|------|--------|
| Admin | `/admin`, `/admin/users`, `/admin/books`, `/admin/statistics` |
| Teacher | `/teacher/batch-borrow`, `/teacher/recommendations`, `/teacher/renew-requests` |
| Student | `/student/recommendations`, `/student/reminders`, `/student/borrow-history` |

## Troubleshooting

- **Can't reach backend** → ensure backend is running on port 8080 and check `vite.config.ts` proxy target
- **Database connection failed** → verify MySQL is running and credentials in `application.yml` match
- **Login fails** → check the database has matching user records in `sys_user` table
- **Python recommendation errors** → ensure `requirements.txt` is installed and `python-command` points to a valid Python executable

## License

For educational purposes.
