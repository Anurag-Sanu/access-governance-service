package com.anurag.access.exception;

public class DuplicateAccessRequestException extends RuntimeException {

    public DuplicateAccessRequestException(String message) {
        super(message);
    }
}
