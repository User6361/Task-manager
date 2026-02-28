package com.main.taskmanager.exception;


/**
 * Эти переопределенные исключения для определенных ошибок в программе
 * Тут обыкновенная ошибка не найденной задачи
 */
public class NotFoundTaskException extends RuntimeException {
    public NotFoundTaskException(String message) {
        super(message);
    }
}
