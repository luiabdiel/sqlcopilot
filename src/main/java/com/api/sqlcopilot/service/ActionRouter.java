package com.api.sqlcopilot.service;

import com.api.sqlcopilot.enums.ActionType;
import com.api.sqlcopilot.exception.PromptTemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class ActionRouter {

    public String buildPrompt(ActionType action, String tables, String message) {
        String template = loadTemplate(action);

        return switch (action) {
            case GENERATE -> template
                    .replace("{tables}", tables != null ? tables : "")
                    .replace("{message}", message);
            case EXPLAIN -> template
                    .replace("{message}", message);
        };
    }

    private String loadTemplate(ActionType action) {
        String fileName = switch (action) {
            case GENERATE -> "prompts/generate.md";
            case EXPLAIN  -> "prompts/explain.md";
        };

        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Prompt template not found: {}", fileName);
            throw new PromptTemplateException(fileName);
        }
    }
}
