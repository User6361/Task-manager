package com.main.taskmanager.exception;

public class NotValidAccessTokenException extends RuntimeException {
    public NotValidAccessTokenException(String message) {
        super(message);
    }
}
