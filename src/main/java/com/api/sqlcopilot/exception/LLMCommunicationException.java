package com.api.sqlcopilot.exception;

public class LLMCommunicationException extends RuntimeException {

    public LLMCommunicationException(String message) {
        super(message);
    }

    public LLMCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
