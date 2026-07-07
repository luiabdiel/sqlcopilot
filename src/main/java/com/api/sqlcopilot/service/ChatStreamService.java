package com.api.sqlcopilot.service;

import com.api.sqlcopilot.dto.ChatRequest;
import com.api.sqlcopilot.dto.ChatResponse;
import com.api.sqlcopilot.shared.sse.SseEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ExecutorService;

@Service
public class ChatStreamService {

    private final ChatService chatService;
    private final ExecutorService sseExecutor;
    private final SseEventPublisher publisher;

    public ChatStreamService(ChatService chatService, ExecutorService sseExecutor, SseEventPublisher publisher) {
        this.chatService = chatService;
        this.sseExecutor = sseExecutor;
        this.publisher = publisher;
    }

    public SseEmitter stream(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(60_000L);
        sseExecutor.execute(() -> process(emitter, request));
        return emitter;
    }

    private void process(SseEmitter emitter, ChatRequest request) {
        try {
            ChatResponse response = chatService.process(request, event ->
                    publisher.send(emitter, Map.of("step", event.step(), "label", event.label()))
            );

            publisher.send(emitter, Map.of(
                    "step", "done",
                    "action", response.action(),
                    "sql", response.sql() == null ? "" : response.sql(),
                    "explanation", response.explanation() == null ? "" : response.explanation()
            ));
            emitter.complete();

        } catch (Exception ex) {
            publisher.send(emitter, Map.of("step", "error", "label", ex.getMessage()));
            emitter.complete();
        }
    }
}
