package com.api.sqlcopilot.controller;

import com.api.sqlcopilot.dto.ChatRequest;
import com.api.sqlcopilot.dto.ChatResponse;
import com.api.sqlcopilot.service.ChatService;
import com.api.sqlcopilot.service.ChatStreamService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping(value = "/api/chat")
public class ChatController {

    private final ChatService service;
    private final ChatStreamService streamService;

    public ChatController(ChatService service, ChatStreamService streamService) {
        this.service = service;
        this.streamService = streamService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat (@RequestBody @Valid ChatRequest request) {
        ChatResponse response = this.service.process(request, event -> {});

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody @Valid ChatRequest request) {
        return streamService.stream(request);
    }
}
