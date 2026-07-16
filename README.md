# Millionaire Mind API

Private Spring Boot API for the Millionaire Mind iPhone app. The app will answer questions using only the book **Jewels of the Millionaire Mind** by Nilo Ortega.

## Stack

- Java 21
- Spring Boot
- Gradle
- Supabase PostgreSQL
- Flyway migrations
- pgvector
- Google Gemini integration

## Local Configuration

Copy `.env.example` to `.env` for local development, but do not commit `.env`.

Required environment variables:

```bash
DB_URL=jdbc:postgresql://db.your-project-ref.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=your_supabase_database_password
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,https://your-frontend-domain.com
GEMINI_API_KEY=your_google_ai_studio_api_key
GEMINI_MODEL=gemini-3.1-flash-lite
```

Gemini is optional at runtime. If `GEMINI_API_KEY` is not set, chat falls back to a local response composed from retrieved book chunks. Google currently offers a Gemini API free tier for certain models, including Flash-family models, but free-tier usage has rate limits and Google states free-tier content may be used to improve its products.

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
  "message": "Generated or fallback answer grounded in saved book chunks.",
  "sources": [
    {
      "title": "Jewels of the Millionaire Mind - chunk 8",
      "excerpt": "Before you decide, pause and ask...",
      "score": 0.75
    }
  ],
  "createdAt": "2026-07-14T00:00:00Z"
}
```

Production frontend URL:

```text
https://millionaire-mind-api.onrender.com/api/chat
```

Chat requests persist conversation history to the `conversations` and `messages` tables. The response returns the conversation ID the frontend should keep sending for follow-up turns.

## Tests

```bash
./gradlew test
```

If a local Gradle wrapper is not present yet, use any installed Gradle 8+ runtime.
