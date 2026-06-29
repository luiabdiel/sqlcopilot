package com.api.sqlcopilot.dto;

import com.api.sqlcopilot.enums.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatRequest(

        @NotNull
        ActionType action,

        @NotBlank
        @Size(max = 3000)
        String message
) {}
