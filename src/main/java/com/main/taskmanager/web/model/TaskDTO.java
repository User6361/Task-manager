package com.main.taskmanager.web.model;

import com.main.taskmanager.task.model.enumclasses.Priority;
import com.main.taskmanager.task.model.enumclasses.TaskStatus;
import lombok.Data;

/**
 * Объект передачи данных (DTO) для сущности {@code Task}.
 * Используется для инкапсуляции и структурированной передачи информации о задаче
 * между различными слоями приложения (например, от базы данных к представлению)
 * или при работе с внешними интерфейсами.
 *
 * Аннотация {@code @Data} от Lombok автоматически генерирует геттеры, сеттеры,
 * {@code equals()}, {@code hashCode()} и {@code toString()}.
 */
@Data
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private TaskStatus status;
    private Long assigneeId;
    private String assigneeName;
    private String createdAt;
    private String updatedAt;
}