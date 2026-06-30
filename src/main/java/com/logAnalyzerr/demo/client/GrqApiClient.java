package com.logAnalyzerr.demo.client;

import tools.jackson.databind.JsonNode;
import com.logAnalyzerr.demo.model.GroqRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class GrqApiClient {

    private final WebClient webClient;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.model}")
    private String model;

    public GrqApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public String analyze(String logContent){
        GroqRequest request = GroqRequest.builder()
                .model(model)
                .maxToken(1024)
                .responseFormat(GroqRequest.ResponseFormat.builder()
                        .type("json_object")
                        .build())
                .messages(List.of(
                        GroqRequest.Message.builder()
                                .role("system")
                                .content( """
                                    you are expert java/spring Boot analyzer.
                                    Analyze the provided logs and respond ONLY with a JSON object in exactly this shape:
                                    {
                                        "summary": "brief summary of what happened",
                                        "errorType": "type of error",
                                        "rootCause": "root cause explanation",
                                        "affectedComponent": "class or service name",
                                        "suggestedFix": "step by step fix",
                                        "severity": "LOW | MEDIUM | HIGH | CRITICAL"
                                    }
                                    Every value MUST be a single plain-text string.
                                    Do NOT nest objects or arrays. For suggestedFix, put all
                                    steps into one string (e.g. "1) ... 2) ... 3) ...").
                                    """)

        .build(),
                GroqRequest.Message.builder()
                        .role("user")
                        .content("analyze these logs: \n\n" + logContent)
                        .build()
                ))
                .build();

        return webClient.post()
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(res -> res

                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText())
                .block();
    }
}
