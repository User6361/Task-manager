package com.main.taskmanager.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация-маркер для включения автоматического логирования AOP.
 * Методы, помеченные этой аннотацией, будут перехвачены аспектом {@link LoggingAspect},
 * что позволяет централизованно отслеживать их выполнение, ошибки и возвращаемые значения.
 */
@Target(ElementType.METHOD) // Указывает, что аннотация может применяться только к методам
@Retention(RetentionPolicy.RUNTIME) // Указывает, что аннотация доступна во время выполнения
public @interface Loggable {
}