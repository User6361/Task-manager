package com.main.taskmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnahtorizedException extends RuntimeException {
    public UnahtorizedException(String message) {
        super(message);
    }
}
