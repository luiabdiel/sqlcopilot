package com.api.sqlcopilot.exception;

public class SchemaNotFoundException extends RuntimeException {

    public SchemaNotFoundException(Long id) {
        super("Schema not found: " + id);
    }
}
