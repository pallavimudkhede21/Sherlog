package com.logAnalyzerr.demo.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Loads the "past incidents" knowledge base from incidents.json (classpath) on
 * startup and stores each entry in the pgvector vector store. Each text is
 * embedded (text -> 384-dim vector) automatically by vectorStore.add(...).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseLoader implements ApplicationRunner {

    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper; // Spring Boot 4 auto-configures a Jackson 3 mapper

    /** Shape of one entry in incidents.json. */
    public record Incident(String text, String errorType, String severity) {}

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Idempotent: if anything is already stored, don't seed again.
        boolean alreadySeeded = !vectorStore.similaritySearch(
                SearchRequest.builder().query("error").topK(1).similarityThresholdAll().build()
        ).isEmpty();
        if (alreadySeeded) {
            log.info("Knowledge base already populated — skipping seed.");
            return;
        }

        Incident[] incidents;
        try (InputStream in = new ClassPathResource("incidents.json").getInputStream()) {
            incidents = objectMapper.readValue(in, Incident[].class);
        }

        List<Document> docs = Arrays.stream(incidents)
                .map(i -> Document.builder()
                        .text(i.text())
                        .metadata(Map.of("errorType", i.errorType(), "severity", i.severity()))
                        .build())
                .toList();

        vectorStore.add(docs); // embeds each text and stores the vectors in pgvector
        log.info("Seeded {} incidents from incidents.json into the vector store.", docs.size());
    }
}
