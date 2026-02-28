package com.main.taskmanager.web.model;

import com.main.taskmanager.task.model.enumclasses.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * Объект передачи данных (DTO), используемый для инкапсуляции данных,
 * полученных от клиента при запросе на создание новой задачи.
 *
 * <p>Аннотация {@code @Data} (Lombok) генерирует стандартные методы (getter, setter, toString, equals, hashCode).</p>
 */
@Data
public class CreateTaskRequest {

    /**
     * Заголовок задачи.
     * Аннотация {@code @NotBlank} обеспечивает, что поле не будет пустым
     * (null или содержать только пробелы) при валидации.
     */
    @NotBlank(message = "Title is required")
    private String title;

    /**
     * Описание задачи.
     */
    private String description;

    /**
     * Приоритет задачи (LOW, MEDIUM, HIGH, URGENT).
     * Соответствует перечислению {@link Priority}.
     */
    private Priority priority;
}