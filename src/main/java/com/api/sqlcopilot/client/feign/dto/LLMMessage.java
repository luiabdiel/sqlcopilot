package com.api.sqlcopilot.client.feign.dto;

public record LLMMessage(
        String role,
        String content
) {}
