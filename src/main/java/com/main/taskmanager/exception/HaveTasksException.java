package com.main.taskmanager.exception;


/**
 * Эти переопределенные исключения для определенных ошибок в программе
 * Тут ошибка должна возникать при попытку удаления пользователя, когда на нем
 * есть таски
 */
public class HaveTasksException extends RuntimeException {
    public HaveTasksException(String message) {
        super(message);
    }
}
