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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;
    private String email;
    private String fullName;

    @ElementCollection(targetClass = Role.class,  fetch = FetchType.EAGER)
    @JoinTable(name =  "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "roles ", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Setter(value = AccessLevel.PUBLIC)
    private Set<Role> roles = new HashSet<>();


    @OneToMany(mappedBy = "assignee", fetch = FetchType.EAGER)
    private List<Task> assignedTasks;






    @Transient
    public int getCountOfTasksWithСertainStatus(TaskStatus status){
        return getAssignedTasks().stream().filter(task -> task.getStatus().equals(status)).toList().size();
    }

    @Transient
    public int getUserTasksWithHighPriority(){
        return getAssignedTasks().stream().filter(task ->
                task.getPriority().equals(Priority.URGENT) ||
                        task.getPriority().equals(Priority.HIGH)).toList().size();
    }



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