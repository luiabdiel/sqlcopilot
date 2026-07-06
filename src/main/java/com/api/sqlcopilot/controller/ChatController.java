package com.api.sqlcopilot.controller;

import com.api.sqlcopilot.documentation.ChatControllerDoc;
import com.api.sqlcopilot.dto.ChatRequest;
import com.api.sqlcopilot.dto.ChatResponse;
import com.api.sqlcopilot.service.ChatService;
import com.api.sqlcopilot.service.ChatStreamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class ChatController implements ChatControllerDoc {

    private final ChatService service;
    private final ChatStreamService streamService;

    public ChatController(ChatService service, ChatStreamService streamService) {
        this.service = service;
        this.streamService = streamService;
    }

    @Override
    public ResponseEntity<ChatResponse> chat (@RequestBody @Valid ChatRequest request) {
        ChatResponse response = this.service.process(request, event -> {});

        return ResponseEntity.ok(response);
    }

    @Override
    public SseEmitter stream(@RequestBody @Valid ChatRequest request) {
        return streamService.stream(request);
    }
}
