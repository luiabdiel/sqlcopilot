package com.api.sqlcopilot.dto;

import com.api.sqlcopilot.enums.ActionType;

public record ChatResponse(
        ActionType action,
        String sql,
        String explanation
) {}
