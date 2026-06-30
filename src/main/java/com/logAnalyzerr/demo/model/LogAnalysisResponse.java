package com.logAnalyzerr.demo.model;

import lombok.Data;

@Data
public class LogAnalysisResponse {

    private String summary;
    private String errorType;
    private String rootCause;
    private String affectedComponent;
    private String suggestedFix;
    private String severity;

}
