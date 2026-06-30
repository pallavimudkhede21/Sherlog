package com.logAnalyzerr.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroqRequest {

    @JsonProperty("model")
    private String model;

    @JsonProperty("max_tokens")
    private int maxToken;

    @JsonProperty("messages")
    private List<Message> messages;

    @JsonProperty("response_format")
    private ResponseFormat responseFormat;

    @Data
    @Builder
    public static class Message {

        @JsonProperty("role")
        private String role;

        @JsonProperty("content")
        private String content;
    }

    @Data
    @Builder
    public static class ResponseFormat {

        @JsonProperty("type")
        private String type;
    }
}
