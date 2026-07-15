# Millionaire Mind API

Private Spring Boot API for the Millionaire Mind iPhone app. The app will answer questions using only the book **Jewels of the Millionaire Mind** by Nilo Ortega.

## Stack

- Java 21
- Spring Boot
- Gradle
- Supabase PostgreSQL
- Flyway migrations
- pgvector
- Future Google Gemini integration

## Local Configuration

Copy `.env.example` to `.env` for local development, but do not commit `.env`.

Required environment variables:

```bash
DB_URL=jdbc:postgresql://db.your-project-ref.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=your_supabase_database_password
```

Gemini and PDF ingestion are intentionally not implemented yet.

## Health Check

```bash
curl http://localhost:8080/api/health
```

Expected response:

```json
{
  "status": "UP",
  "application": "millionaire-mind-api"
}
```

## Tests

```bash
./gradlew test
```

If a local Gradle wrapper is not present yet, use any installed Gradle 8+ runtime.
