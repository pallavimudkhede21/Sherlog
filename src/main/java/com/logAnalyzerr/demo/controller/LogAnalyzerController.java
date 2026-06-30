package com.logAnalyzerr.demo.controller;

import com.logAnalyzerr.demo.model.LogAnalysisRequest;
import com.logAnalyzerr.demo.model.LogAnalysisResponse;
import com.logAnalyzerr.demo.service.LogAnalyzerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogAnalyzerController {

    private final LogAnalyzerService service;
    private final VectorStore vectorStore;

    @PostMapping("/analyze")
    public ResponseEntity<LogAnalysisResponse> analyze(
            @RequestBody LogAnalysisRequest request,
            @RequestParam(defaultValue = "true") boolean rag ) {
        log.info("Received log analysis request (rag={})", rag);

        if(request.getLogs() == null || request.getLogs().isBlank()){
            return  ResponseEntity.badRequest().build();
        }
        LogAnalysisResponse response = service.analyze(request, rag);
        return  ResponseEntity.ok(response);
    }

    /**
     * Demo endpoint: embed the query, then return the most semantically similar
     * past incidents from pgvector (with their similarity score). Lets us watch
     * retrieval work before we wire it into the prompt (Step 4).
     */
    @GetMapping("/similar")
    public ResponseEntity<List<Map<String, Object>>> similar(
            @RequestParam String q,
            @RequestParam(defaultValue = "3") int k) {
        List<Document> matches = vectorStore.similaritySearch(
                SearchRequest.builder().query(q).topK(k).build());
        List<Map<String, Object>> out = matches.stream()
                .map(d -> Map.<String, Object>of(
                        "score", d.getScore(),
                        "errorType", d.getMetadata().getOrDefault("errorType", ""),
                        "severity", d.getMetadata().getOrDefault("severity", ""),
                        "content", d.getText()))
                .toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health(){
        return ResponseEntity.ok("Log Analyzer is Running!");
    }
}
