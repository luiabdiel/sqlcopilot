package com.api.sqlcopilot.dto;

import com.api.sqlcopilot.enums.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequest(

        @NotNull
        ActionType action,

        @NotBlank
        String message
) {}
