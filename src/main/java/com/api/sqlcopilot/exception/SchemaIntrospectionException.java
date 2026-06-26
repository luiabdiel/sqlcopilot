package com.api.sqlcopilot.exception;

public class SchemaIntrospectionException extends RuntimeException {

    public SchemaIntrospectionException(Throwable cause) {
        super("Failed to introspect database schema", cause);
    }
}
