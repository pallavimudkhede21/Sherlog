# 🔍 Log Analyzer AI — Backend

An AI-powered log analyzer. Paste an application log (a stack trace or error), and a
Large Language Model (**Groq**) diagnoses the error and returns a **structured,
step-by-step fix** — grounded in a knowledge base of past incidents using **RAG**
(Retrieval-Augmented Generation).

> **In one sentence:** it reads your logs the way a senior engineer would, and answers
> with clean JSON your tools (or a UI) can display.

---

## What it produces

Send this log:

```
java.lang.NullPointerException at com.demo.UserService.getUser(UserService.java:42)
  - Cannot invoke getName() because "user" is null
```

Get back this:

```json
{
  "summary": "NullPointerException in UserService.getUser due to a null user object.",
  "errorType": "NullPointerException",
  "rootCause": "The user object is null at retrieval, likely a failed lookup.",
  "affectedComponent": "UserService",
  "suggestedFix": "1) Null-check the user in getUser() 2) verify the DB query 3) return a safe default.",
  "severity": "CRITICAL"
}
```

---

## How it works

```
                         ┌──────────────────────────────────────────────┐
   POST /api/logs/analyze │                LogAnalyzerService            │
   { "logs": "..." }  ───▶│                                              │
                          │   1. embed the log  ──► local MiniLM model   │
                          │   2. similarity search ──► pgvector (top-3)  │
                          │   3. inject matched past incidents into the  │
                          │      prompt  (QuestionAnswerAdvisor / RAG)   │
                          │   4. ask Groq (JSON mode)                    │
                          │   5. map JSON ──► LogAnalysisResponse        │
                          └──────────────────────────────────────────────┘
                                              │
                                     structured JSON (200 OK)
```

- **Embeddings** run **locally** (ONNX `all-MiniLM-L6-v2`, 384-dim) — no embedding API, no cost.
- **Vector search** uses **pgvector** (Postgres) with an HNSW cosine index.
- **RAG** means the LLM answers using *your* past incidents, not just its general training.

---

## Tech stack

| Layer | Technology |
|-------|-----------|
| Language / runtime | **Java 21** |
| Framework | **Spring Boot 4.1** (Spring Web MVC) |
| AI framework | **Spring AI 2.0** (`ChatClient`, structured output, advisors) |
| LLM | **Groq** — `llama-3.1-8b-instant` (OpenAI-compatible API) |
| Embeddings | **Spring AI Transformers** (local ONNX `all-MiniLM-L6-v2`) |
| Vector store | **PostgreSQL + pgvector** (via Docker) |
| Build | **Maven** (wrapper included) |

---

## Prerequisites

- **JDK 21** (Spring Boot 4 requires Java 17+; this project uses 21).
- **Docker** (to run Postgres + pgvector).
- A **Groq API key** — free at <https://console.groq.com/keys>.

---

## Setup & run

### 1. Configure your API key
Create a file named **`.env`** in this folder (it is git-ignored — never commit it):

```
GROQ_API_KEY=gsk_your_key_here
```

The app reads it via Spring's native config import (see `application.yaml`).

### 2. Start the vector database (Postgres + pgvector)
```bash
docker run -d --name pgvector-db \
  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=lograg \
  -p 5432:5432 -v pgvector_data:/var/lib/postgresql/data \
  pgvector/pgvector:pg17
```
On first startup the app auto-creates the `vector_store` table and seeds it from
[`src/main/resources/incidents.json`](src/main/resources/incidents.json).

### 3. Run the app
Make sure `JAVA_HOME` points at a **JDK 21**, then:

```bash
# Windows (PowerShell)
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

The API starts on **http://localhost:8080**. Open **http://localhost:8080/** for the
built-in static UI, or use the [React frontend](frontend/README.md).

---

## API reference

### `POST /api/logs/analyze`
Analyze a log and return a structured diagnosis.

| Query param | Default | Description |
|-------------|---------|-------------|
| `rag` | `true` | Toggle Retrieval-Augmented Generation on/off |
| `severity` | *(none)* | Only retrieve past incidents of this severity (`LOW`/`MEDIUM`/`HIGH`/`CRITICAL`) |

```bash
curl -X POST "http://localhost:8080/api/logs/analyze?rag=true" \
  -H "Content-Type: application/json" \
  -d '{"logs":"java.lang.OutOfMemoryError: Java heap space"}'
```

### `GET /api/logs/similar`
Semantic search over the knowledge base (shows retrieval without the LLM).

| Query param | Default | Description |
|-------------|---------|-------------|
| `q` | *(required)* | The text to search for |
| `k` | `3` | How many results |
| `severity` | *(none)* | Filter results to this severity |

```bash
curl "http://localhost:8080/api/logs/similar?q=app%20hung%20under%20load&k=3"
```

### `GET /api/logs/health`
Liveness check → `Log Analyzer is Running!`

---

## The knowledge base

The "past incidents" live in [`src/main/resources/incidents.json`](src/main/resources/incidents.json)
— a simple array of `{ text, errorType, severity }`. To grow the knowledge base, **edit
that file**. To reload it after editing (the loader is idempotent), clear the table and
restart:

```bash
docker exec pgvector-db psql -U postgres -d lograg -c "TRUNCATE TABLE vector_store;"
```

---

## Project structure

```
src/main/java/com/logAnalyzerr/demo/
  DemoApplication.java              # Spring Boot entry point
  controller/LogAnalyzerController  # REST endpoints
  service/LogAnalyzerService        # ChatClient + RAG + structured output
  rag/KnowledgeBaseLoader           # seeds pgvector from incidents.json on startup
  model/                            # LogAnalysisRequest / LogAnalysisResponse
src/main/resources/
  application.yaml                  # Groq + datasource + vector store config
  incidents.json                    # the knowledge base
  static/index.html                 # built-in no-build UI
frontend/                           # React (Vite) UI — see frontend/README.md
```

---

## Troubleshooting

| Symptom | Cause / fix |
|---------|-------------|
| `unclosed string literal` at build | `JAVA_HOME` points at Java 8. Point it at **JDK 21**. |
| `Could not resolve placeholder 'GROQ_API_KEY'` | Create the `.env` file with your key (step 1). |
| `404 Unknown request URL ... /openai/chat/completions` | The base URL must include `/v1`; already set in `application.yaml`. |
| `model_decommissioned` (HTTP 400) | Groq retired the model. Use a current one (e.g. `llama-3.1-8b-instant`). |
| App fails to connect to `localhost:5432` | The pgvector container isn't running (step 2). |

---

## Security note
The `.env` file holds a secret and is git-ignored. Never commit real keys. If a key is
ever exposed, **rotate it** at <https://console.groq.com/keys>.
