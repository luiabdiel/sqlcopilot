package com.api.sqlcopilot.client.feign.dto;

import java.util.List;

public record LLMResponse(
        List<LLMChoice> choices
) {}
