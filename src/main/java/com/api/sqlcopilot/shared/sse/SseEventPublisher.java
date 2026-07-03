package com.api.sqlcopilot.shared.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Component
public class SseEventPublisher {

    public void send(SseEmitter emitter, Object payload) {
        try {
            emitter.send(SseEmitter.event().data(payload));
        } catch (IOException ignored) {} // cliente desconectou, nada a fazer
    }
}
