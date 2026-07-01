package com.api.sqlcopilot.exception;

import com.api.sqlcopilot.exception.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SchemaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSchemaNotFound(SchemaNotFoundException ex) {
        log.error("Schema not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Schema Not Found",
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(LLMCommunicationException.class)
    public ResponseEntity<ErrorResponse> handleLLMCommunication(LLMCommunicationException ex) {
        log.error("LLM communication error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                new ErrorResponse(
                        HttpStatus.BAD_GATEWAY.value(),
                        "LLM Communication Error",
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        log.error("Validation error: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation Error",
                        message,
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(PromptTemplateException.class)
    public ResponseEntity<ErrorResponse> handlePromptTemplate(PromptTemplateException ex) {
        log.error("Prompt template error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Prompt Template Error",
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(SchemaIntrospectionException.class)
    public ResponseEntity<ErrorResponse> handleSchemaIntrospection(SchemaIntrospectionException ex) {
        log.error("Schema introspection error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Schema Introspection Error",
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(ForbiddenSqlException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenSql(ForbiddenSqlException ex) {
        log.error("Forbidden SQL detected: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        "Forbidden SQL",
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(UnsupportedActionException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedAction(UnsupportedActionException ex) {
        log.error("Unsupported action requested: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Unsupported Action",
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An unexpected error occurred",
                        LocalDateTime.now()
                )
        );
    }
}
