package com.api.sqlcopilot.client.feign.dto;

import java.util.List;

public record LLMRequest(
        String model,
        List<LLMMessage> messages,
        Integer max_tokens,
        Double temperature
) {}
