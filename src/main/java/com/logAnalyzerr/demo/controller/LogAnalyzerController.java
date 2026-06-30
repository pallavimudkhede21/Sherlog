package com.logAnalyzerr.demo.controller;

import com.logAnalyzerr.demo.model.LogAnalysisRequest;
import com.logAnalyzerr.demo.model.LogAnalysisResponse;
import com.logAnalyzerr.demo.service.LogAnalyzerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogAnalyzerController {

    private final LogAnalyzerService service;

    @PostMapping("/analyze")
    public ResponseEntity<LogAnalysisResponse> analyze(
            @RequestBody LogAnalysisRequest request ) {
        log.info("Received log analysis request");

        if(request.getLogs() == null || request.getLogs().isBlank()){
            return  ResponseEntity.badRequest().build();
        }
        LogAnalysisResponse response = service.analyze(request);
        return  ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health(){
        return ResponseEntity.ok("Log Analyzer is Running!");
    }
}
