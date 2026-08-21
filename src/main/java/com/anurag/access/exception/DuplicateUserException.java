package com.anurag.access.exception;

public class DuplicateUserException extends RuntimeException{

    public DuplicateUserException(String message) {
        super(message);
    }
}
