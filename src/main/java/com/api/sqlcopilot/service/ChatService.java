package com.api.sqlcopilot.service;

import com.api.sqlcopilot.client.feign.LLMClient;
import com.api.sqlcopilot.client.feign.dto.LLMMessage;
import com.api.sqlcopilot.client.feign.dto.LLMRequest;
import com.api.sqlcopilot.client.feign.dto.LLMResponse;
import com.api.sqlcopilot.dto.ChatRequest;
import com.api.sqlcopilot.dto.ChatResponse;
import com.api.sqlcopilot.enums.ActionType;
import com.api.sqlcopilot.exception.LLMCommunicationException;
import com.api.sqlcopilot.utils.SqlValidatorUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ChatService {

    private final ActionRouter action;
    private final LLMClient client;
    private final SchemaIntrospectionService introspection;

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.llm-model}")
    private String model;

    public ChatService(LLMClient client, ActionRouter action, SchemaIntrospectionService introspection) {
        this.client = client;
        this.action = action;
        this.introspection = introspection;
    }

    @CircuitBreaker(name = "llm-client", fallbackMethod = "fallback")
    public ChatResponse process(ChatRequest request) {
        log.info("Processing request: action={}", request.action());

        if (request.message().length() > 3000) {
            throw new IllegalArgumentException("Prompt too large");
        }

        String tables = null;

        if (request.action() == ActionType.GENERATE) {
            tables = introspection.introspect();
        }
        String prompt = this.action.buildPrompt(request.action(), tables, request.message());

        LLMRequest llmRequest = new LLMRequest(
                model,
                List.of(new LLMMessage("user", prompt)),
                512,
                0.1
        );

        try {
            LLMResponse llmResponse = this.client.send("Bearer " + apiKey, llmRequest);

            if (llmResponse == null || llmResponse.choices() == null || llmResponse.choices().isEmpty()) {
                throw new LLMCommunicationException("LLM returned an empty response");
            }

            String content = llmResponse.choices().getFirst().message().content();

            if (content == null || content.isBlank()) {
                throw new LLMCommunicationException("LLM returned an empty content");
            }

            if (request.action() == ActionType.GENERATE) {
                SqlValidatorUtils.validate(content);

                return new ChatResponse(request.action(), content, null);
            }

            return new ChatResponse(request.action(), null, content);

        } catch (Exception ex) {
            throw new LLMCommunicationException("Failed to communicate with LLM", ex);
        }
    }

    public ChatResponse fallback(ChatRequest request, Exception ex) {
        log.error("Circuit breaker open — LLM unavailable: {}", ex.getMessage());
        throw new LLMCommunicationException("LLM temporarily unavailable. Try again later.");
    }
}
