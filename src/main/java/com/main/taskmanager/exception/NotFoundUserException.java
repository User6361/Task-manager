package com.main.taskmanager.exception;


/**
 * Эти переопределенные исключения для определенных ошибок в программе
 * Тут обыкновенная ошибка "Не найден пользователь"
 */
public class NotFoundUserException extends RuntimeException {
    public NotFoundUserException(String message) {
        super(message);
    }
}
