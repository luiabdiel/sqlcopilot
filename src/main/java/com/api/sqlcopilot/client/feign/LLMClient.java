package com.api.sqlcopilot.client.feign;

import com.api.sqlcopilot.client.feign.dto.LLMRequest;
import com.api.sqlcopilot.client.feign.dto.LLMResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "llm-client")
public interface LLMClient {

    @PostMapping("/chat/completions")
    LLMResponse send(@RequestHeader("Authorization") String authorization,
                     @RequestBody LLMRequest request);
}
