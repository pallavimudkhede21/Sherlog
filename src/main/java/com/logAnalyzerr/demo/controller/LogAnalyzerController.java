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
import org.springframework.util.StringUtils;
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

    /**
     * @param rag      toggle Retrieval-Augmented Generation (default true).
     * @param severity optional metadata filter — restrict RAG retrieval to incidents of this severity.
     */
    @PostMapping("/analyze")
    public ResponseEntity<LogAnalysisResponse> analyze(
            @RequestBody LogAnalysisRequest request,
            @RequestParam(defaultValue = "true") boolean rag,
            @RequestParam(required = false) String severity ) {
        log.info("Received log analysis request (rag={}, severity={})", rag, severity);

        if(request.getLogs() == null || request.getLogs().isBlank()){
            return  ResponseEntity.badRequest().build();
        }
        LogAnalysisResponse response = service.analyze(request, rag, severity);
        return  ResponseEntity.ok(response);
    }

    /**
     * Embed the query and return the most semantically similar past incidents.
     * Pass severity to restrict the search to that severity (metadata filter).
     */
    @GetMapping("/similar")
    public ResponseEntity<List<Map<String, Object>>> similar(
            @RequestParam String q,
            @RequestParam(defaultValue = "3") int k,
            @RequestParam(required = false) String severity) {
        SearchRequest.Builder search = SearchRequest.builder().query(q).topK(k);
        if (StringUtils.hasText(severity)) {
            search = search.filterExpression("severity == '" + severity + "'");
        }
        List<Document> matches = vectorStore.similaritySearch(search.build());
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
