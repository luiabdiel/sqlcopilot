package com.api.sqlcopilot.exception;

import com.api.sqlcopilot.exception.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void whenSchemaNotFoundExceptionThenReturn404() {
        SchemaNotFoundException ex = new SchemaNotFoundException(404L);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleSchemaNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Schema Not Found", response.getBody().error());
    }

    @Test
    void whenLLMCommunicationExceptionThenReturn502() {
        LLMCommunicationException ex = new LLMCommunicationException("LLM indisponível");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleLLMCommunication(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(502, response.getBody().status());
        assertEquals("LLM Communication Error", response.getBody().error());
        assertEquals("LLM indisponível", response.getBody().message());
    }

    @Test
    void whenPromptTemplateExceptionThenReturn500() {
        PromptTemplateException ex = new PromptTemplateException("Template não encontrado");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePromptTemplate(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Prompt Template Error", response.getBody().error());
        assertEquals("Template não encontrado", response.getBody().message());
    }

    @Test
    void whenSchemaIntrospectionExceptionThenReturn500() {
        SchemaIntrospectionException ex = new SchemaIntrospectionException(new RuntimeException("Erro ao ler schema"));

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleSchemaIntrospection(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Schema Introspection Error", response.getBody().error());
        assertEquals("Failed to introspect database schema", response.getBody().message());
    }

    @Test
    void whenForbiddenSqlExceptionThenReturn403() {
        ForbiddenSqlException ex = new ForbiddenSqlException("SQL proibido detectado");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleForbiddenSql(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().status());
        assertEquals("Forbidden SQL", response.getBody().error());
        assertEquals("SQL proibido detectado", response.getBody().message());
    }

    @Test
    void whenUnsupportedActionExceptionThenReturn400() {
        UnsupportedActionException ex = new UnsupportedActionException("Ação não suportada");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUnsupportedAction(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Unsupported Action", response.getBody().error());
        assertEquals("Ação não suportada", response.getBody().message());
    }

    @Test
    void whenGenericExceptionThenReturn500() {
        Exception ex = new Exception("erro inesperado");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }

    @Test
    void whenLLMCommunicationExceptionWithCauseThenReturn502() {
        Throwable cause = new RuntimeException("timeout");
        LLMCommunicationException ex = new LLMCommunicationException("LLM indisponível", cause);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleLLMCommunication(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(502, response.getBody().status());
        assertEquals("LLM Communication Error", response.getBody().error());
        assertEquals("LLM indisponível", response.getBody().message());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void whenMethodArgumentNotValidExceptionThenReturn400WithFieldError() {
        FieldError fieldError = new FieldError("chatRequest", "message", "must not be blank");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Validation Error", response.getBody().error());
        assertEquals("message: must not be blank", response.getBody().message());
    }

    @Test
    void whenMethodArgumentNotValidExceptionWithNoFieldErrorsThenReturnDefaultMessage() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation error", response.getBody().message());
    }
}