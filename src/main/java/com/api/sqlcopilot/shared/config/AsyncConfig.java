package com.api.sqlcopilot.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    // Máximo de requisições SSE processadas em paralelo antes de enfileirar
    @Value("${sse.thread-pool-size}")
    private int SSE_THREAD_POOL_SIZE;

    @Bean
    public ExecutorService sseExecutor() {
        return Executors.newFixedThreadPool(SSE_THREAD_POOL_SIZE);
    }
}
