package com.logAnalyzerr.demo.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Seeds the pgvector store with a small "past incidents" knowledge base on startup.
 * Each Document's text is embedded (text -> 384-dim vector) and stored automatically
 * by Spring AI when we call vectorStore.add(...).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseLoader implements ApplicationRunner {

    private final VectorStore vectorStore;

    @Override
    public void run(ApplicationArguments args) {
        // Idempotent: if anything is already stored, don't seed again.
        boolean alreadySeeded = !vectorStore.similaritySearch(
                SearchRequest.builder().query("error").topK(1).similarityThresholdAll().build()
        ).isEmpty();
        if (alreadySeeded) {
            log.info("Knowledge base already populated — skipping seed.");
            return;
        }

        List<Document> incidents = List.of(
            Document.builder()
                .text("NullPointerException occurs when code calls a method on a null object, "
                    + "often an entity returned from a repository lookup that found no row. "
                    + "Resolution: null-check the value (or use Optional.orElseThrow) and confirm "
                    + "the database query actually returns a record before dereferencing it.")
                .metadata(Map.of("errorType", "NullPointerException", "severity", "HIGH"))
                .build(),
            Document.builder()
                .text("HikariPool 'Connection is not available, request timed out' means the JDBC "
                    + "connection pool is exhausted under load. Resolution: increase "
                    + "spring.datasource.hikari.maximum-pool-size, ensure connections are closed, "
                    + "and investigate slow or long-running transactions holding connections.")
                .metadata(Map.of("errorType", "ConnectionTimeout", "severity", "CRITICAL"))
                .build(),
            Document.builder()
                .text("OutOfMemoryError: Java heap space usually comes from loading too much data at "
                    + "once (huge result sets) or unbounded in-memory caches. Resolution: raise the "
                    + "-Xmx heap, page large queries, bound caches with eviction, and capture a heap "
                    + "dump to locate the leak.")
                .metadata(Map.of("errorType", "OutOfMemoryError", "severity", "CRITICAL"))
                .build(),
            Document.builder()
                .text("Application fails to start with 'Web server failed to start. Port 8080 was "
                    + "already in use' (BindException). Another process holds the port. Resolution: "
                    + "stop that process or set server.port to a free port in application.yaml.")
                .metadata(Map.of("errorType", "PortInUse", "severity", "MEDIUM"))
                .build(),
            Document.builder()
                .text("LazyInitializationException: could not initialize proxy - no Session. A lazy "
                    + "JPA association is accessed outside an open Hibernate session. Resolution: "
                    + "access it inside a @Transactional method, use a JOIN FETCH query, or map to a "
                    + "DTO projection.")
                .metadata(Map.of("errorType", "LazyInitializationException", "severity", "HIGH"))
                .build(),
            Document.builder()
                .text("Calls to an external API return 401 Unauthorized or 403 Forbidden due to a "
                    + "missing, wrong, or expired credential. Resolution: verify the API key or bearer "
                    + "token, the exact header name (Authorization: Bearer ...), and that the token "
                    + "has not expired.")
                .metadata(Map.of("errorType", "AuthError", "severity", "HIGH"))
                .build()
        );

        vectorStore.add(incidents); // embeds each text and stores the vectors in pgvector
        log.info("Seeded {} incidents into the vector store.", incidents.size());
    }
}
