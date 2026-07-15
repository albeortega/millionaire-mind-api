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
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,https://your-frontend-domain.com
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

## Chat API

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"What is the first millionaire mind principle?"}'
```

Request body:

```json
{
  "conversationId": "optional-existing-conversation-uuid",
  "message": "User question"
}
```

The endpoint also accepts the frontend chat history payload:

```json
{
  "messages": [
    {
      "role": "user",
      "content": "User question"
    }
  ]
}
```

Response body:

```json
{
  "conversationId": "generated-or-existing-conversation-uuid",
  "role": "ASSISTANT",
  "message": "The chat API is ready. RAG retrieval and Gemini generation will be connected next.",
  "sources": [],
  "createdAt": "2026-07-14T00:00:00Z"
}
```

Production frontend URL:

```text
https://millionaire-mind-api.onrender.com/api/chat
```

## Tests

```bash
./gradlew test
```

If a local Gradle wrapper is not present yet, use any installed Gradle 8+ runtime.
