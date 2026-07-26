# Schema Drift Detector

Detects breaking vs. safe schema changes between two snapshots of a database
table, with an AI-generated plain-English explanation for anything risky.

## What's implemented so far

- Data model: `Source`, `SchemaSnapshot`, `SchemaField`, `Comparison`, `FieldChange`
- **Diff engine** (`DiffEngineService`) — the core logic. Classifies every
  field-level change as SAFE or BREAKING:
  - Field removed → breaking
  - Field added, nullable → safe
  - Field added, required, no default → breaking
  - Type changed → breaking, unless it's a known-safe widening (e.g. INT → BIGINT)
  - Nullable → NOT NULL → breaking
  - NOT NULL → nullable → safe
- Unit tests proving each rule (`DiffEngineServiceTest`)
- Schema capture from a live MySQL table via `INFORMATION_SCHEMA`
- Gemini AI integration to explain breaking changes in plain English
  (falls back to the rule-based reason if the AI call fails)
- REST endpoints: sources, snapshots, comparisons

- JWT authentication: register/login (`AuthController`), password hashing
  (BCrypt via `UserService`), token generation/validation (`JwtService`),
  and a request filter (`JwtAuthFilter`) that protects every route except
  `/api/auth/**`

## Still to build

- React frontend
- Deployment

## Run it

```bash
# Requires Java 17+, Maven, and a MySQL instance for the app's own DB
mvn spring-boot:run
```

Update `src/main/resources/application.properties` with your MySQL
credentials and Gemini API key before running.

## Run the tests

```bash
mvn test
```

This runs the diff engine test suite without needing a database — it's pure
logic, which is exactly why it's the strongest part of this project to
demo and discuss.

## API quick reference

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Create an account, returns a JWT |
| POST | `/api/auth/login` | Log in, returns a JWT |
| GET | `/api/sources` | List sources for current user |
| POST | `/api/sources` | Create a new source |
| GET | `/api/sources/{id}/snapshots` | List snapshots for a source |
| POST | `/api/sources/{id}/snapshots` | Capture a new snapshot from a live MySQL table |
| POST | `/api/comparisons` | Run a comparison between two snapshot IDs |
| GET | `/api/comparisons/by-source/{sourceId}` | Comparison history for a source |
| GET | `/api/comparisons/{id}` | Full detail of one comparison |
