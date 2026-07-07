package com.api.sqlcopilot.service;

import com.api.sqlcopilot.dto.ChatRequest;
import com.api.sqlcopilot.dto.ChatResponse;
import com.api.sqlcopilot.dto.ProgressEvent;
import com.api.sqlcopilot.enums.ActionType;
import com.api.sqlcopilot.shared.sse.SseEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatStreamServiceTest {

    @InjectMocks
    private ChatStreamService chatStreamService;

    @Mock
    private ChatService chatService;

    @Mock
    private ExecutorService sseExecutor;

    @Mock
    private SseEventPublisher publisher;

    private void runSynchronously() {
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(sseExecutor).execute(any());
    }

    @Test
    void whenStreamThenReturnSseEmitter() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste clientes");

        SseEmitter result = chatStreamService.stream(request);

        assertNotNull(result);
    }

    @Test
    void whenStreamWithValidRequestThenSendDoneEvent() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste clientes");
        ChatResponse response = new ChatResponse(ActionType.GENERATE, "SELECT * FROM clientes", "Retorna todos os clientes");

        runSynchronously();
        when(chatService.process(eq(request), any())).thenReturn(response);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        chatStreamService.stream(request);

        verify(publisher).send(any(SseEmitter.class), captor.capture());

        Map<?, ?> payload = (Map<?, ?>) captor.getValue();
        assertEquals("done", payload.get("step"));
        assertEquals("SELECT * FROM clientes", payload.get("sql"));
        assertEquals("Retorna todos os clientes", payload.get("explanation"));
        assertEquals(ActionType.GENERATE, payload.get("action"));
    }

    @Test
    void whenStreamWithNullSqlAndExplanationThenSendEmptyStrings() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste clientes");
        ChatResponse response = new ChatResponse(ActionType.GENERATE, null, null);

        runSynchronously();
        when(chatService.process(eq(request), any())).thenReturn(response);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        chatStreamService.stream(request);

        verify(publisher).send(any(SseEmitter.class), captor.capture());

        Map<?, ?> payload = (Map<?, ?>) captor.getValue();
        assertEquals("", payload.get("sql"));
        assertEquals("", payload.get("explanation"));
    }

    @Test
    void whenChatServiceThrowsExceptionThenSendErrorEvent() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste clientes");

        runSynchronously();
        when(chatService.process(eq(request), any())).thenThrow(new RuntimeException("LLM error"));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        chatStreamService.stream(request);

        verify(publisher).send(any(SseEmitter.class), captor.capture());

        Map<?, ?> payload = (Map<?, ?>) captor.getValue();
        assertEquals("error", payload.get("step"));
        assertEquals("LLM error", payload.get("label"));
    }

    @Test
    void whenChatServiceEmitsProgressEventThenPublishToEmitter() {
        ChatRequest request = new ChatRequest(ActionType.GENERATE, "Liste clientes");
        ChatResponse response = new ChatResponse(ActionType.GENERATE, "SELECT 1", null);

        runSynchronously();

        doAnswer(inv -> {
            Consumer<ProgressEvent> onProgress = inv.getArgument(1);
            onProgress.accept(new ProgressEvent("validating", "Validando a pergunta"));
            return response;
        }).when(chatService).process(eq(request), any());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        chatStreamService.stream(request);

        verify(publisher, times(2)).send(any(SseEmitter.class), captor.capture());

        Map<?, ?> progressPayload = (Map<?, ?>) captor.getAllValues().get(0);
        assertEquals("validating", progressPayload.get("step"));
        assertEquals("Validando a pergunta", progressPayload.get("label"));
    }
}