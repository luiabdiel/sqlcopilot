package com.api.sqlcopilot.documentation;

import com.api.sqlcopilot.dto.ChatRequest;
import com.api.sqlcopilot.dto.ChatResponse;
import com.api.sqlcopilot.exception.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Chat", description = "Endpoints para geração e explicação de queries SQL via LLM")
@RequestMapping("/api/chat")
public interface ChatControllerDoc {

    @Operation(
            summary = "Processar mensagem",
            description = "Processa uma mensagem e retorna a query SQL gerada ou a explicação, dependendo do ActionType informado (GENERATE ou EXPLAIN)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resposta processada com sucesso",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida (validação ou ação não suportada)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "SQL proibido detectado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Schema não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Limite de requisições excedido (10 req/min por IP)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Erro de comunicação com o LLM",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    ResponseEntity<ChatResponse> chat(@RequestBody @Valid ChatRequest request);

    @Operation(
            summary = "Processar mensagem com streaming (SSE)",
            description = "Processa a mensagem e retorna os eventos em tempo real via Server-Sent Events (SSE)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stream iniciado com sucesso",
                    content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter stream(@RequestBody @Valid ChatRequest request);
}
