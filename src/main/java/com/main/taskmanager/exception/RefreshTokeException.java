package com.main.taskmanager.exception;

public class RefreshTokeException extends RuntimeException {

    public RefreshTokeException(String token, String message) {
        super(message.formatted("Error trying to refresh by token: {0} : {1} ",  token, message));
    }

    public RefreshTokeException(String message) {
        super(message);
    }
}
