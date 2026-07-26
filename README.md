 Schema Drift Detector — Backend

Spring Boot API that compares two database schema snapshots and flags each
change as SAFE or BREAKING. Breaking changes get a plain-English AI
explanation.

Frontend repo: https://github.com/umabharathi03032/Schema-drift-frontend

## Tech stack

Java 17, Spring Boot, MySQL, JWT auth, Gemini AI

## Features

- Capture a table's schema from a live MySQL database
- Compare two snapshots and classify each field change (added, removed,
  type changed, nullability changed) as safe or breaking
- AI explanation for breaking changes, with rule-based fallback
- JWT-based login/register

## Run it

1. Set your MySQL and Gemini details in `src/main/resources/application.properties`
2. Run:
   ```bash
   mvn spring-boot:run
   ```
3. API runs at `http://localhost:8080`

## Run tests

```bash
mvn test
```

## Main API endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | /api/auth/register | Create account |
| POST | /api/auth/login | Log in |
| GET/POST | /api/sources | List / create sources |
| GET/POST | /api/sources/{id}/snapshots | List / capture snapshots |
| POST | /api/comparisons | Compare two snapshots |
| GET | /api/comparisons/{id} | View one comparison |
