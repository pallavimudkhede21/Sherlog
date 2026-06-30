package com.logAnalyzerr.demo.service;

import com.logAnalyzerr.demo.model.LogAnalysisRequest;
import com.logAnalyzerr.demo.model.LogAnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LogAnalyzerService {

    private final ChatClient chat;
    private final VectorStore vectorStore;

    private static final String SYSTEM_PROMPT = """
            You are an expert Java and Spring Boot log analyst.
            Analyze the provided logs: identify the single most important error,
            its root cause, the affected component, and a concrete step-by-step fix.
            If similar past incidents are provided as context, use their proven
            resolutions to ground your fix. Respond with JSON only.
            Severity must be one of: LOW, MEDIUM, HIGH, CRITICAL.
            """;

    public LogAnalyzerService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chat = builder.build();
        this.vectorStore = vectorStore;
    }

    public LogAnalysisResponse analyze(LogAnalysisRequest request) {
        return analyze(request, true);
    }

    /**
     * @param useRag when true, retrieve similar past incidents from the vector store
     *               and inject them into the prompt before the model answers.
     *               When false, the model sees only the raw log (no grounding).
     */
    public LogAnalysisResponse analyze(LogAnalysisRequest request, boolean useRag) {
        try {
            ChatClient.ChatClientRequestSpec spec = chat.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(u -> u.text("Analyze these logs:\n\n{logs}")
                                .param("logs", request.getLogs()))
                    .options(OpenAiChatOptions.builder()
                            .responseFormat(OpenAiChatModel.ResponseFormat.builder()
                                    .type(OpenAiChatModel.ResponseFormat.Type.JSON_OBJECT)
                                    .build()));

            if (useRag) {
                spec = spec.advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder().topK(3).build())
                        .build());
            }

            return spec.call().entity(LogAnalysisResponse.class);
        } catch (Exception e) {
            log.error("Failed to analyze logs with Spring AI", e);
            LogAnalysisResponse error = new LogAnalysisResponse();
            error.setSummary("Failed to analyze logs");
            error.setErrorType("AnalysisError");
            error.setRootCause(e.getMessage());
            error.setAffectedComponent("LogAnalyzerService");
            error.setSuggestedFix("Check the Groq API key, model name, and network connectivity");
            error.setSeverity("HIGH");
            return error;
        }
    }
}
