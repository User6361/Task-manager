package com.main.taskmanager.task.model.enumclasses;

/**
 * Перечисление, определяющее уровень приоритета задачи.
 * Используется для ранжирования задач и автоматического расчета дедлайна (см. {@link com.main.taskmanager.task.model.Task}).
 */
public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}