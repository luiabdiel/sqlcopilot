package com.api.sqlcopilot.service;

import com.api.sqlcopilot.client.feign.LLMCachedClient;
import com.api.sqlcopilot.dto.ChatRequest;
import com.api.sqlcopilot.dto.ChatResponse;
import com.api.sqlcopilot.dto.ProgressEvent;
import com.api.sqlcopilot.enums.ActionType;
import com.api.sqlcopilot.exception.LLMCommunicationException;
import com.api.sqlcopilot.exception.UnsupportedActionException;
import com.api.sqlcopilot.shared.utils.PromptGuardUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Slf4j
public class ChatService {

    private final ActionRouter action;
    private final LLMCachedClient client;
    private final SchemaIntrospectionService introspection;

    public ChatService(ActionRouter action, LLMCachedClient client, SchemaIntrospectionService introspection) {
        this.action = action;
        this.client = client;
        this.introspection = introspection;
    }

    @CircuitBreaker(name = "llm-client", fallbackMethod = "fallback")
    public ChatResponse process(ChatRequest request, Consumer<ProgressEvent> onProgress) {
        log.info("Processing request: action={}", request.action());

        if (request.action() != ActionType.GENERATE) {
            throw new UnsupportedActionException("Ação '" + request.action() + "' ainda não está disponível.");
        }

        if (request.message().length() > 3000) {
            throw new IllegalArgumentException("Prompt too large");
        }

        onProgress.accept(new ProgressEvent("validating", "Validando a pergunta"));
        PromptGuardUtils.validateUserIntent(request.message());

        onProgress.accept(new ProgressEvent("schema", "Lendo schema do banco"));
        String tables = introspection.introspect();
        String prompt = this.action.buildPrompt(request.action(), tables, request.message());

        onProgress.accept(new ProgressEvent("generating", "Gerando SQL com a IA"));
        return this.client.send(request, prompt);
    }

    public ChatResponse fallback(ChatRequest request, Consumer<ProgressEvent> onProgress, Throwable ex) {
        log.error("Circuit breaker open — LLM unavailable: {}", ex.getMessage());
        throw new LLMCommunicationException("LLM temporarily unavailable. Try again later.");
    }
}
