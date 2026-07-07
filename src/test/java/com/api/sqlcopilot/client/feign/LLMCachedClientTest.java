package com.api.sqlcopilot.client.feign;

import com.api.sqlcopilot.client.feign.dto.LLMChoice;
import com.api.sqlcopilot.client.feign.dto.LLMMessage;
import com.api.sqlcopilot.client.feign.dto.LLMResponse;
import com.api.sqlcopilot.dto.ChatRequest;
import com.api.sqlcopilot.dto.ChatResponse;
import com.api.sqlcopilot.enums.ActionType;
import com.api.sqlcopilot.exception.LLMCommunicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LLMCachedClientTest {

    @InjectMocks
    private LLMCachedClient llmCachedClient;

    @Mock
    private LLMClient llmClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(llmCachedClient, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(llmCachedClient, "model", "test-model");
    }

    @Test
    void whenLLMReturnsValidSelectThenReturnChatResponse() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste clientes");
        LLMResponse llmResponse = buildLLMResponse("SELECT * FROM clientes;");

        when(llmClient.send(any(), any())).thenReturn(llmResponse);

        ChatResponse result = llmCachedClient.send(request, "prompt");

        assertNotNull(result);
        assertEquals(ActionType.GENERATE, result.action());
        assertEquals("SELECT * FROM clientes;", result.sql());
    }

    @Test
    void whenLLMReturnsNullResponseThenThrowLLMCommunicationException() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste clientes");
        when(llmClient.send(any(), any())).thenReturn(null);

        LLMCommunicationException ex = assertThrows(
                LLMCommunicationException.class,
                () -> llmCachedClient.send(request, "prompt")
        );

        assertEquals("LLM returned an empty response", ex.getMessage());
    }

    @Test
    void whenLLMReturnsNullChoicesThenThrowLLMCommunicationException() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste clientes");
        when(llmClient.send(any(), any())).thenReturn(new LLMResponse(null));

        LLMCommunicationException ex = assertThrows(
                LLMCommunicationException.class,
                () -> llmCachedClient.send(request, "prompt")
        );

        assertEquals("LLM returned an empty response", ex.getMessage());
    }

    @Test
    void whenLLMReturnsEmptyChoicesThenThrowLLMCommunicationException() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste clientes");
        when(llmClient.send(any(), any())).thenReturn(new LLMResponse(List.of()));

        LLMCommunicationException ex = assertThrows(
                LLMCommunicationException.class,
                () -> llmCachedClient.send(request, "prompt")
        );

        assertEquals("LLM returned an empty response", ex.getMessage());
    }

    @Test
    void whenLLMReturnsNullContentThenThrowLLMCommunicationException() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste clientes");
        when(llmClient.send(any(), any())).thenReturn(buildLLMResponse(null));

        LLMCommunicationException ex = assertThrows(
                LLMCommunicationException.class,
                () -> llmCachedClient.send(request, "prompt")
        );

        assertEquals("LLM returned an empty content", ex.getMessage());
    }

    @Test
    void whenLLMReturnsBlankContentThenThrowLLMCommunicationException() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste clientes");
        when(llmClient.send(any(), any())).thenReturn(buildLLMResponse("   "));

        LLMCommunicationException ex = assertThrows(
                LLMCommunicationException.class,
                () -> llmCachedClient.send(request, "prompt")
        );

        assertEquals("LLM returned an empty content", ex.getMessage());
    }

    @Test
    void whenLLMClientThrowsExceptionThenWrapInLLMCommunicationException() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste clientes");
        when(llmClient.send(any(), any())).thenThrow(new RuntimeException("connection refused"));

        LLMCommunicationException ex = assertThrows(
                LLMCommunicationException.class,
                () -> llmCachedClient.send(request, "prompt")
        );

        assertEquals("Failed to communicate with LLM", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex.getCause());
    }

    private LLMResponse buildLLMResponse(String content) {
        return new LLMResponse(List.of(new LLMChoice(new LLMMessage("assistant", content))));
    }
}