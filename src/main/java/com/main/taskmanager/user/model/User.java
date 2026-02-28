package com.main.taskmanager.user.model;

import com.main.taskmanager.task.model.Task;
import com.main.taskmanager.task.model.enumclasses.Priority;
import com.main.taskmanager.task.model.enumclasses.TaskStatus;
import com.main.taskmanager.user.model.enumclasses.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * Сущность JPA, представляющая пользователя в системе.
 * Реализует интерфейс {@link UserDetails} Spring Security, что позволяет
 * использовать этот класс напрямую в механизмах аутентификации.
 *
 * <p>Аннотации Lombok {@code @Getter}, {@code @Setter}, {@code @Builder} и т.д.
 * используются для автоматической генерации стандартных методов.</p>
 */
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User implements UserDetails {

    /** Уникальный идентификатор пользователя, генерируется автоматически. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Логин пользователя. Должен быть уникальным и не может быть null. */
    @Column(unique = true, nullable = false)
    private String username;

    /** Хешированный пароль пользователя. Не может быть null. */
    @Column(nullable = false)
    private String password;

    /** Электронная почта пользователя. */
    private String email;

    /** Полное имя пользователя (для отображения). */
    private String fullName;

    /** Роль пользователя (например, ADMINISTRATOR, USER). Хранится в БД как сет из ролей. */
    @ElementCollection(targetClass = Role.class,  fetch = FetchType.EAGER)
    @JoinTable(name =  "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "roles ", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();


    /**
     * Список задач, назначенных этому пользователю.
     * Связь One-to-Many: один пользователь может иметь много задач.
     * {@code mappedBy = "assignee"} указывает на поле в сущности {@link Task}.
     * {@code FetchType.EAGER} используется для немедленной загрузки задач.
     *
     */
    @OneToMany(mappedBy = "assignee", fetch = FetchType.EAGER)
    private List<Task> assignedTasks;

    // ================= Методы UserDetails =================

    /**
     * Возвращает разрешения, предоставленные пользователю (роли).
     * В данной реализации права доступа управляются через поле {@link #role},
     * а этот метод возвращает пустую коллекцию, полагаясь на внешнюю проверку.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // В рамках простого Spring Security, роль может быть представлена как GrantedAuthority.
        // Здесь используется упрощенный подход, полагаясь на прямое поле 'role'.
        return Collections.emptyList();
    }

    /**
     * Возвращает имя пользователя (логин), используемое для аутентификации.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Возвращает пароль, используемый для аутентификации.
     */
    @Override
    public String getPassword() {
        return password;
    }

    // Эти методы возвращают true по умолчанию, указывая на то, что учетная запись активна.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    // ================= Вспомогательные методы для UI/статистики =================

    /**
     * Подсчитывает количество назначенных задач с определенным статусом.
     *
     * @param status Требуемый статус задачи (например, OPEN, COMPLETED).
     * @return Количество задач с данным статусом.
     */
    @Transient
    public int getCountOfTasksWithСertainStatus(TaskStatus status){
        return getAssignedTasks().stream().filter(task -> task.getStatus().equals(status)).toList().size();
    }

    /**
     * Подсчитывает количество назначенных задач с высоким (HIGH) или срочным (URGENT) приоритетом.
     *
     * @return Количество срочных задач.
     */
    @Transient
    public int getUserTasksWithHighPriority(){
        return getAssignedTasks().stream().filter(task ->
                task.getPriority().equals(Priority.URGENT) ||
                        task.getPriority().equals(Priority.HIGH)).toList().size();
    }


    /**
     * Возвращает строковое представление объекта, исключая пароль и включая только
     * количество назначенных задач для предотвращения рекурсивного вызова.
     * @return Строковое представление пользователя.
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", Roles='" + roles.toString() + '\'' +
                ", assignedTasks=" + (assignedTasks != null ? assignedTasks.size() : 0) + // Только количество задач
                '}';
    }
}