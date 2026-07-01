# 🔍 Sherlog

**An AI log detective that finds the root cause of your errors — and suggests the fix.**

Paste a stack trace or error log; a Groq LLM returns a structured diagnosis, grounded in a
knowledge base of past incidents using **RAG** (Retrieval-Augmented Generation).

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen)
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0-blue)
![pgvector](https://img.shields.io/badge/pgvector-Postgres-336791)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## Features

- 🧠 **AI root-cause diagnosis** — turns a raw stack trace into a summary, root cause, affected component, severity, and a step-by-step fix.
- 📚 **RAG (Retrieval-Augmented Generation)** — grounds every answer in a searchable knowledge base of past incidents, so fixes reflect real, proven resolutions.
- 🔎 **Semantic search with pgvector** — finds similar past errors by *meaning*, not keywords, using a PostgreSQL vector database (HNSW + cosine).
- 🏠 **Local embeddings** — an in-process ONNX model (`all-MiniLM-L6-v2`) turns text into vectors with no external embedding API and no cost.
- 🧩 **Structured JSON output** — Spring AI maps the LLM reply straight onto a typed Java object, no brittle parsing.
- ⚡ **Fast inference** — powered by Groq's OpenAI-compatible API (`llama-3.1-8b-instant`).
- 🎛️ **Tunable retrieval** — toggle RAG on/off and filter retrieved incidents by severity.

## Example

**Input** — a raw log:
```
java.lang.NullPointerException at com.demo.UserService.getUser(UserService.java:42)
```

**Output** — structured JSON:
```json
{
  "summary": "NullPointerException in UserService.getUser due to a null user.",
  "errorType": "NullPointerException",
  "rootCause": "The user object is null at retrieval.",
  "affectedComponent": "UserService",
  "suggestedFix": "Null-check the user, verify the DB query, return a safe default.",
  "severity": "CRITICAL"
}
```

## How it works

```
log → embed (local MiniLM) → search pgvector (top-3 incidents) → inject into prompt → Groq (JSON mode) → typed response
```

- Embeddings run **locally** (ONNX `all-MiniLM-L6-v2`, 384-dim) — no embedding API, no cost.
- Retrieval uses **pgvector** with an HNSW cosine index.
- **RAG** grounds answers in *your* past incidents, not just the model's training.

## Tech stack

| | |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 4.1 (Spring Web MVC) |
| AI | Spring AI 2.0 + Groq (`llama-3.1-8b-instant`) |
| Embeddings | Local ONNX `all-MiniLM-L6-v2` |
| Vector store | PostgreSQL + pgvector (Docker) |

## Getting started

**Prerequisites:** JDK 21 · Docker · a free [Groq API key](https://console.groq.com/keys).

1. **Add your key** — create a `.env` file (git-ignored):
   ```
   GROQ_API_KEY=gsk_your_key_here
   ```
2. **Start pgvector:**
   ```bash
   docker run -d --name pgvector-db \
     -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=lograg \
     -p 5432:5432 pgvector/pgvector:pg17
   ```
3. **Run** (with `JAVA_HOME` pointing at JDK 21):
   ```bash
   ./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run
   ```
   The API starts on **http://localhost:8080**. On first run it seeds the vector store from
   `src/main/resources/incidents.json`.

## API

| Method | Endpoint | Notes |
|--------|----------|-------|
| `POST` | `/api/logs/analyze?rag=&severity=` | Diagnose a log. `rag` toggles retrieval; `severity` filters which incidents are used. |
| `GET`  | `/api/logs/similar?q=&k=&severity=` | Semantic search over the knowledge base. |
| `GET`  | `/api/logs/health` | Liveness check. |

```bash
curl -X POST "http://localhost:8080/api/logs/analyze" \
  -H "Content-Type: application/json" \
  -d '{"logs":"java.lang.OutOfMemoryError: Java heap space"}'
```

## Knowledge base

Past incidents live in `src/main/resources/incidents.json` (`{ text, errorType, severity }`).
Edit it to grow the knowledge base; to reload, run
`TRUNCATE TABLE vector_store;` in the container and restart.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `unclosed string literal` at build | `JAVA_HOME` is on Java 8 — use JDK 21. |
| `Could not resolve placeholder 'GROQ_API_KEY'` | Create the `.env` file. |
| Can't connect to `localhost:5432` | Start the pgvector container. |

## Security

`.env` is git-ignored — never commit API keys. Rotate any exposed key at
<https://console.groq.com/keys>.

## License

[MIT](LICENSE) © 2026 Pallavi Mudkhede

---

<sub>**Keywords:** AI log analyzer · Retrieval-Augmented Generation (RAG) in Java · Spring AI 2.0 example · Spring Boot 4 · Groq LLM · pgvector · vector search · semantic search · local ONNX embeddings · root cause analysis · structured LLM output · QuestionAnswerAdvisor · Java AI tutorial</sub>
