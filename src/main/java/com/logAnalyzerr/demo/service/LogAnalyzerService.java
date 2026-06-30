package com.logAnalyzerr.demo.service;

import tools.jackson.databind.ObjectMapper;
import com.logAnalyzerr.demo.client.GrqApiClient;
import com.logAnalyzerr.demo.model.LogAnalysisRequest;
import com.logAnalyzerr.demo.model.LogAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogAnalyzerService {

    private final GrqApiClient groqClient;
    private final ObjectMapper objectMapper;

    public LogAnalysisResponse analyze(LogAnalysisRequest request) {
       try{
           String rawJson = groqClient.analyze(request.getLogs());
           return  objectMapper.readValue(rawJson, LogAnalysisResponse.class);
       }catch (Exception e){
           log.error("Failed to parse Groq response", e);
           LogAnalysisResponse errorResponse = new LogAnalysisResponse();
           errorResponse.setSummary("Failed to analyze logs");
           errorResponse.setErrorType("AnalysisError");
           errorResponse.setRootCause(e.getMessage());
           errorResponse.setAffectedComponent("LogAnalyzerService");
           errorResponse.setSuggestedFix("Check Groq API Key and Network connectivity");
           errorResponse.setSeverity("HIGH");
           return errorResponse;
       }

    }
}
