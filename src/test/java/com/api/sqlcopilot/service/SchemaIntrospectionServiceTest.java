package com.api.sqlcopilot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchemaIntrospectionServiceTest {

    @InjectMocks
    private SchemaIntrospectionService schemaIntrospectionService;

    @Test
    void whenIntrospectThenReturnSchemaContent() {
        String result = schemaIntrospectionService.introspect();

        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void whenIntrospectThenReturnContentContainingKnownTable() {
        String result = schemaIntrospectionService.introspect();

        assertTrue(result.contains("customers"));
    }

    @Test
    void whenSchemaFileNotFoundThenThrowIOException() {
        try (var ignored = mockConstruction(
                ClassPathResource.class,
                (mock, context) -> when(mock.getContentAsString(any()))
                        .thenThrow(new IOException("schema not found"))
        )) {
            assertThrows(
                    IOException.class,
                    () -> new SchemaIntrospectionService()
            );
        }
    }
}