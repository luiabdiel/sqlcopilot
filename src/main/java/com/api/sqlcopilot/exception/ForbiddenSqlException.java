package com.api.sqlcopilot.exception;

public class ForbiddenSqlException extends RuntimeException {

    public ForbiddenSqlException(String message) {
        super(message);
    }
}
