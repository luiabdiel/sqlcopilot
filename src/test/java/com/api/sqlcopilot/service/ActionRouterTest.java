package com.api.sqlcopilot.service;

import com.api.sqlcopilot.enums.ActionType;
import com.api.sqlcopilot.exception.PromptTemplateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionRouterTest {

    @InjectMocks
    private ActionRouter actionRouter;

    @Test
    void whenBuildPromptWithGenerateActionThenContainsTables() {
        String tables = "clientes(id, nome)";
        String message = "Liste todos os clientes";

        String result = actionRouter.buildPrompt(ActionType.GENERATE, tables, message);

        assertTrue(result.contains(tables));
    }

    @Test
    void whenBuildPromptWithGenerateActionThenContainsMessage() {
        String message = "Liste todos os clientes";

        String result = actionRouter.buildPrompt(ActionType.GENERATE, "clientes(id, nome)", message);

        assertTrue(result.contains(message));
    }

    @Test
    void whenBuildPromptWithGenerateAndNullTablesThenReplacesWithEmptyString() {
        String message = "Liste todos os clientes";

        String result = actionRouter.buildPrompt(ActionType.GENERATE, null, message);

        assertFalse(result.contains("{tables}"));
        assertTrue(result.contains(message));
    }

    @Test
    void whenBuildPromptWithGenerateActionThenDoesNotContainPlaceholders() {
        String result = actionRouter.buildPrompt(ActionType.GENERATE, "clientes(id, nome)", "Liste clientes");

        assertFalse(result.contains("{tables}"));
        assertFalse(result.contains("{message}"));
    }

    @Test
    void whenBuildPromptWithExplainActionThenContainsMessage() {
        String message = "SELECT * FROM clientes;";

        String result = actionRouter.buildPrompt(ActionType.EXPLAIN, null, message);

        assertTrue(result.contains(message));
    }

    @Test
    void whenBuildPromptWithExplainActionThenDoesNotContainPlaceholders() {
        String result = actionRouter.buildPrompt(ActionType.EXPLAIN, null, "SELECT 1;");

        assertFalse(result.contains("{message}"));
    }

    @Test
    void whenTemplateFileNotFoundThenThrowPromptTemplateException() throws IOException {
        try (var ignored = mockConstruction(
                ClassPathResource.class,
                (mock, context) -> when(mock.getContentAsString(any())).thenThrow(new IOException("file not found"))
        )) {
            assertThrows(
                    PromptTemplateException.class,
                    () -> actionRouter.buildPrompt(ActionType.GENERATE, "clientes(id, nome)", "Liste clientes")
            );
        }
    }
}