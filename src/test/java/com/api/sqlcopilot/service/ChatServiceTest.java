package com.api.sqlcopilot.service;

import com.api.sqlcopilot.client.feign.LLMCachedClient;
import com.api.sqlcopilot.dto.ChatRequest;
import com.api.sqlcopilot.dto.ChatResponse;
import com.api.sqlcopilot.dto.ProgressEvent;
import com.api.sqlcopilot.enums.ActionType;
import com.api.sqlcopilot.exception.ForbiddenSqlException;
import com.api.sqlcopilot.exception.LLMCommunicationException;
import com.api.sqlcopilot.exception.UnsupportedActionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ActionRouter actionRouter;

    @Mock
    private LLMCachedClient llmCachedClient;

    @Mock
    private SchemaIntrospectionService schemaIntrospectionService;

    @Mock
    @SuppressWarnings("unchecked")
    private Consumer<ProgressEvent> onProgress;

    @Test
    void whenValidGenerateRequestThenReturnChatResponse() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste todos os clientes");
        ChatResponse expected = new ChatResponse(ActionType.GENERATE, "SELECT * FROM clientes", null);

        when(schemaIntrospectionService.introspect()).thenReturn("clientes(id, nome)");
        when(actionRouter.buildPrompt(ActionType.GENERATE, "clientes(id, nome)", request.message())).thenReturn("prompt gerado");
        when(llmCachedClient.send(request, "prompt gerado")).thenReturn(expected);

        ChatResponse result = chatService.process(request, onProgress);

        assertEquals(expected, result);
    }

    @Test
    void whenValidGenerateRequestThenEmitProgressEventsInOrder() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste todos os clientes");

        when(schemaIntrospectionService.introspect()).thenReturn("clientes(id, nome)");
        when(actionRouter.buildPrompt(any(), any(), any())).thenReturn("prompt");
        when(llmCachedClient.send(any(), any())).thenReturn(new ChatResponse(ActionType.GENERATE, "SELECT 1", null));

        ArgumentCaptor<ProgressEvent> captor = ArgumentCaptor.forClass(ProgressEvent.class);

        chatService.process(request, onProgress);

        verify(onProgress, times(3)).accept(captor.capture());
        List<ProgressEvent> events = captor.getAllValues();
        assertEquals("validating", events.get(0).step());
        assertEquals("schema",     events.get(1).step());
        assertEquals("generating", events.get(2).step());
    }

    @Test
    void whenActionIsExplainThenThrowUnsupportedActionException() {
        ChatRequest request = new ChatRequest(ActionType.EXPLAIN, "Explique essa query");

        UnsupportedActionException ex = assertThrows(
                UnsupportedActionException.class,
                () -> chatService.process(request, onProgress)
        );

        assertTrue(ex.getMessage().contains("EXPLAIN"));
        verifyNoInteractions(schemaIntrospectionService, actionRouter, llmCachedClient);
    }

    @Test
    void whenMessageOver3000CharsThenThrowIllegalArgumentException() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "a".repeat(3001));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> chatService.process(request, onProgress)
        );

        assertEquals("Prompt too large", ex.getMessage());
        verifyNoInteractions(schemaIntrospectionService, actionRouter, llmCachedClient);
    }

    @Test
    void whenMessageContainsForbiddenKeywordThenThrowForbiddenSqlException() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "delete todos os registros");

        assertThrows(
                ForbiddenSqlException.class,
                () -> chatService.process(request, onProgress)
        );
        verifyNoInteractions(schemaIntrospectionService, actionRouter, llmCachedClient);
    }

    @Test
    void whenFallbackCalledThenThrowLLMCommunicationException() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste clientes");
        Exception cause = new RuntimeException("timeout");

        LLMCommunicationException ex = assertThrows(
                LLMCommunicationException.class,
                () -> chatService.fallback(request, onProgress, cause)
        );

        assertEquals("LLM temporarily unavailable. Try again later.", ex.getMessage());
    }
}