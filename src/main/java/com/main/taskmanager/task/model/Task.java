package com.main.taskmanager.task.model;

import com.main.taskmanager.task.model.enumclasses.Priority;
import com.main.taskmanager.task.model.enumclasses.TaskStatus;
import com.main.taskmanager.user.model.User;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Сущность JPA, представляющая собой задачу в системе управления задачами.
 * Содержит данные о заголовке, описании, статусе, приоритете и назначенном пользователе.
 * Аннотации {@code @PrePersist} и {@code @PreUpdate} используются для автоматического управления временем создания/обновления.
 */
@Table("users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Task {

    // Временные поля и форматировщик, которые не сохраняются в БД.
    private final static LocalDateTime NOW = LocalDateTime.now();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Уникальный идентификатор задачи, генерируется автоматически. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Заголовок задачи. Обязательное поле. */
    private String title;

    /** Описание задачи. */
    @Size(min = 1, max = 500)
    private String description;


    private Priority priority;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private User assignee;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;



    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TaskStatus.OPEN;
        }
    }


    public LocalDate getDeadline() {
        int days = switch (priority) {
            case LOW -> 30;
            case MEDIUM -> 7;
            case HIGH -> 3;
            case URGENT -> 1;
        };

        return createdAt.toLocalDate().plusDays(days);
    }

    public String getCreateDate() {
        return createdAt.format(formatter);
    }

    public String getUpdatedDate(){
        return updatedAt.format(formatter);
    }


    public long getDaysLeft() {
        if( ChronoUnit.DAYS.between(LocalDate.now(), getDeadline()) <= 0) {return 0; }
        return ChronoUnit.DAYS.between(LocalDate.now(), getDeadline());
    }

    public String getDeadlineColor() {
        long days = getDaysLeft();
        if (this.getStatus() == TaskStatus.COMPLETED){
            return "success";
        } else if (days >= 7) {
            return "primary";
        } else if (days >= 3){
            return "warning";
        }

        return "danger";
    }


    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", assignee=" + (assignee != null ? assignee.getId() : null) + // Только ID пользователя
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}