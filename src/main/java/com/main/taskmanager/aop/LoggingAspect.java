package com.main.taskmanager.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Аспект для логирования вызовов методов, помеченных аннотацией {@link Loggable}.
 * Использует различные типы Advice (советов) для записи информации о выполнении,
 * возвращаемых значениях и исключениях в лог.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Совет, выполняемый ПЕРЕД выполнением метода, помеченного @Loggable.
     * Используется для записи в лог информации о начале работы метода.
     * * @param joinPoint Точка соединения, предоставляющая информацию о вызываемом методе.
     */
    @Before("annotation(com.main.taskmanager.aop.Loggable)")
    public void logBefore(JoinPoint joinPoint) {
        log.info("До выполнения методода: {}", joinPoint.getSignature().getName());
    }

    /**
     * Совет, выполняемый ПОСЛЕ завершения метода, помеченного @Loggable (независимо от успеха/ошибки).
     * * @param joinPoint Точка соединения.
     */
    @After("@annotation(com.main.taskmanager.aop.Loggable)")
    public void logAfter(JoinPoint joinPoint) {
        log.info("После выполнение метода: {}", joinPoint.getSignature().getName());
    }

    /**
     * Совет, выполняемый ПОСЛЕ успешного возврата из метода, помеченного @Loggable.
     * Записывает в лог возвращенное методом значение.
     * * @param joinPoint Точка соединения.
     * @param result Результат, возвращенный методом.
     */
    @AfterReturning(pointcut = "@annotation(com.main.taskmanager.aop.Loggable)", returning = "result")
    public void logAfterReturn(JoinPoint joinPoint, Object result) {
        log.info("После выполнения из метода: {}, получили результат {}", joinPoint.getSignature().getName(), result);
    }

    /**
     * Совет, выполняемый ПОСЛЕ того, как метод, помеченный @Loggable, выбросил исключение.
     * Записывает в лог информацию об исключении.
     * * @param joinPoint Точка соединения.
     * @param exception Выброшенное исключение.
     */
    @AfterThrowing(pointcut = "@annotation(com.main.taskmanager.aop.Loggable)", throwing = "exception")
    public void  logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        log.info("После вызова из метода: {}, получили ошибку: {}",  joinPoint.getSignature().getName(), exception);
    }

    /**
     * Совет, окружающий выполнение метода, помеченного @Loggable.
     * Позволяет контролировать выполнение, измерять время, и гарантирует выполнение
     * кода до и после основного метода.
     * * @param joinPoint Точка соединения, допускающая продолжение (proceed).
     * @return Результат выполнения метода.
     * @throws Throwable Если метод выбрасывает исключение.
     */
    @Around("@annotation(com.main.taskmanager.aop.Loggable)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Метод: {}, был вызван", joinPoint.getSignature().getName());
        Object result = joinPoint.proceed(); // Вызов основного метода

        log.info("Метод: {}, быз закончен",  joinPoint.getSignature().getName());
        return result;
    }
}