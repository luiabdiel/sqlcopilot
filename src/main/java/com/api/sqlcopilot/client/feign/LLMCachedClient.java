package com.api.sqlcopilot.client.feign;

import com.api.sqlcopilot.client.feign.dto.LLMMessage;
import com.api.sqlcopilot.client.feign.dto.LLMRequest;
import com.api.sqlcopilot.client.feign.dto.LLMResponse;
import com.api.sqlcopilot.dto.ChatRequest;
import com.api.sqlcopilot.dto.ChatResponse;
import com.api.sqlcopilot.exception.LLMCommunicationException;
import com.api.sqlcopilot.shared.utils.SqlValidatorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LLMCachedClient {

    private final LLMClient client;

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.llm-model}")
    private String model;

    public LLMCachedClient(LLMClient client) {
        this.client = client;
    }

    @Cacheable(value = "llm-responses", key = "#request.action().name() + ':' + #request.message().trim().toLowerCase()")
    public ChatResponse send(ChatRequest request, String prompt) {
        log.info("Cache miss — calling LLM: action={}", request.action());

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

            SqlValidatorUtils.validate(content);

            return new ChatResponse(request.action(), content, null);
        } catch (LLMCommunicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LLMCommunicationException("Failed to communicate with LLM", ex);
        }
    }
}
