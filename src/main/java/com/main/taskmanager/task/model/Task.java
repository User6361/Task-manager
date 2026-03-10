package com.main.taskmanager.task.model;

import com.main.taskmanager.task.model.enumclasses.Priority;
import com.main.taskmanager.task.model.enumclasses.TaskStatus;
import com.main.taskmanager.user.model.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Сущность JPA, представляющая собой задачу в системе управления задачами.
 * Содержит данные о заголовке, описании, статусе, приоритете и назначенном пользователе.
 * Аннотации {@code @PrePersist} и {@code @PreUpdate} используются для автоматического управления временем создания/обновления.
 */
@Entity
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Task {

    // Временные поля и форматировщик, которые не сохраняются в БД.
    @Transient
    private final static LocalDateTime NOW = LocalDateTime.now();
    @Transient
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Уникальный идентификатор задачи, генерируется автоматически. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Заголовок задачи. Обязательное поле. */
    @Column(nullable = false)
    @Size(min = 1, max = 255)
    private String title;

    /** Описание задачи. */
    @Size(min = 1, max = 500)
    private String description;

    /** Приоритет задачи. Хранится в БД как строка. */
    @Enumerated(EnumType.STRING)
    private Priority priority;

    /** Текущий статус задачи. Хранится в БД как строка. */
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    /**
     * Пользователь, ответственный за выполнение задачи.
     * Связь Many-to-One: много задач могут быть назначены одному пользователю.
     * {@code FetchType.EAGER} означает, что пользователь загружается вместе с задачей.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    /** Дата и время создания задачи. */
    private LocalDateTime createdAt;

    /** Дата и время последнего обновления задачи. */
    private LocalDateTime updatedAt;


    /**
     * Метод-callback, вызываемый JPA перед сохранением (INSERT) сущности.
     * Устанавливает {@code createdAt} и {@code updatedAt}, а также устанавливает
     * статус по умолчанию {@link TaskStatus#OPEN}, если он не задан.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TaskStatus.OPEN;
        }
    }


    /**
     * Рассчитывает предполагаемый дедлайн задачи, основанный на ее приоритете.
     * (e.g., URGENT = 1 день, LOW = 30 дней).
     * @return Предполагаемая дата дедлайна (LocalDate).
     */
    @Transient
    public LocalDate getDeadline() {
        int days = switch (priority) {
            case LOW -> 30;
            case MEDIUM -> 7;
            case HIGH -> 3;
            case URGENT -> 1;
        };

        return createdAt.toLocalDate().plusDays(days);
    }

    /**
     * Возвращает дату создания задачи в отформатированном строковом виде.
     * @return Отформатированная дата создания.
     */
    @Transient
    public String getCreateDate() {
        return createdAt.format(formatter);
    }

    /**
     * Возвращает дату последнего обновления задачи в отформатированном строковом виде.
     * @return Отформатированная дата обновления.
     */
    @Transient
    public String getUpdatedDate(){
        return updatedAt.format(formatter);
    }


    /**
     * Рассчитывает количество дней, оставшихся до дедлайна.
     * @return Количество оставшихся дней (0, если дедлайн прошел).
     */
    @Transient
    public long getDaysLeft() {
        if( ChronoUnit.DAYS.between(LocalDate.now(), getDeadline()) <= 0) {return 0; }
        return ChronoUnit.DAYS.between(LocalDate.now(), getDeadline());
    }

    /**
     * Определяет цветовой код для отображения дедлайна в UI, основанный на статусе и оставшемся времени.
     * <p>
     * <ul>
     * <li>SUCCESS: Завершена.</li>
     * <li>PRIMARY: Больше 7 дней.</li>
     * <li>WARNING: От 3 до 6 дней.</li>
     * <li>DANGER: 2 дня или меньше (включая просроченные).</li>
     * </ul>
     * @return Строка с названием CSS-класса ("success", "primary", "warning", "danger").
     */
    @Transient
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


    /**
     * Метод-callback, вызываемый JPA перед обновлением (UPDATE) сущности.
     * Обновляет поле {@code updatedAt} текущим временем.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Возвращает строковое представление объекта, включая только ID назначенного пользователя
     * для предотвращения рекурсивных вызовов.
     * @return Строковое представление задачи.
     */
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