package com.main.taskmanager.user.service;

import com.main.taskmanager.task.model.Task;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.model.enumclasses.Role;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Интерфейс сервисного слоя для управления сущностью {@link User}.
 * Расширяет {@link UserDetailsService}, обеспечивая интеграцию с механизмами
 * аутентификации Spring Security.
 */
public interface UserService {

    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    List<Task> getAllTasksOfUser(Long userId);

    Optional<Task> getUserTaskById(Long id);

    Optional<User> getUserByUsername(String username);

    User createUser(String username, String password, String email, String fullName, Set<Role> roles, User currentUser);

    User createUserInit(String username, String password, String email, String fullName, Set<Role> roles);

    User createUserRegester(String username, String password, String email, String fullName, Set<Role> roles);

    boolean userExists(String username);

    void deleteUser(Long id, User currentUser);

    User updateUser(Long id, String username, String password, String email, String fullName, Set<Role> roles, User currentUser);

    User updateUserWithoutPassword(Long id, String username,String email, String fullName, Set<Role> roles, User currentUser);
}