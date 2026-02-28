package com.main.taskmanager.exception;

/**
 * Эти переопределенные исключения для определенных ошибок в программе
 * Тут обыкновенная ошибка "Нет прав"
 */
public class NoRightsException extends RuntimeException {
    public NoRightsException(String message) {
        super(message);
    }
}
