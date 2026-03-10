package com.main.taskmanager.user.service.impl;

import com.main.taskmanager.aop.Loggable;
import com.main.taskmanager.exception.HaveTasksException;
import com.main.taskmanager.exception.NoRightsException;
import com.main.taskmanager.exception.NotFoundUserException;
import com.main.taskmanager.task.model.Task;
import com.main.taskmanager.task.model.enumclasses.Priority;
import com.main.taskmanager.task.model.enumclasses.TaskStatus;
import com.main.taskmanager.task.repository.TaskRepository;
import com.main.taskmanager.user.model.enumclasses.Role;
import com.main.taskmanager.user.service.UserService;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.repository.UserRepository;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Основная реализация сервиса {@link UserService} для управления пользователями.
 * Отвечает за:
 * <ul>
 * <li>Аутентификацию (через {@link #loadUserByUsername(String)}) и хеширование паролей.</li>
 * <li>Проверку прав (только {@link Role#ADMINISTRATOR} может создавать/удалять/обновлять).</li>
 * <li>Обработку исключений, связанных с безопасностью и бизнес-правилами (например, удаление пользователя с задачами).</li>
 * </ul>
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DatabaseUserService implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskRepository taskRepository;


    @Override
    @Loggable
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    @Override
    @Loggable
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }


    @Override
    public List<Task> getAllTasksOfUser(Long userId) {
        if(getUserById(userId).isPresent()) {
            // NOTE: Использование findAll() для последующей фильтрации в памяти неэффективно.
            // Лучше использовать findByAssignee() в репозитории задач.
            return taskRepository.findAll().stream().filter(task -> task.getAssignee().getId().equals(userId)).toList();
        }
        log.info("UserService: getAllTasks() -> no tasks with user id - " + userId);
        return new ArrayList<>();
    }


    @Override
    public Optional<Task> getUserTaskById(Long taskId) {
        if(taskRepository.findById(taskId).isPresent()) {
            return taskRepository.findById(taskId);
        }
        log.info("UserService: getUserTaskById -> no task with id - " + taskId);
        return Optional.empty();
    }

    @Override
    @Loggable
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    @Override
    @Transactional
    @Loggable
    public User createUser(String username, String password, String email, String fullName, Set<Role> roles, User currentUser) {
        if(!currentUser.getRoles().stream().anyMatch(Role.ADMINISTRATOR::equals)) {
            log.info("UserService: createUser() -> user dont have permision to create user!");
            throw new NoRightsException("User dont have permision to create user!");
        }
        return createUserDatabase(username, password, email, fullName, roles);
    }


    @Override
    @Transactional
    @Loggable
    public User createUserInit(String username, String password, String email, String fullName, Set<Role> roles) {
        return createUserDatabase(username, password, email, fullName, roles);
    }

    @Override
    @Transactional
    @Loggable
    public User createUserRegester(String username, String password, String email, String fullName, Set<Role> roles) {
        return createUserDatabase(username, password, email, fullName, roles);
    }



    @Override
    @Loggable
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }



    @Override
    public void deleteUser(Long userId, User currentUser){
        if(!userRepository.findById(userId).isPresent()){
            throw new UsernameNotFoundException("User not found: " + userId);
        }
        if(!currentUser.getRoles().stream().anyMatch(Role.ADMINISTRATOR::equals)) {
            log.info("UserService: deleteUser() -> user dont have permision to delete user!");
            throw new NoRightsException("User dont have permision to delete user!");
        }
        // Защита системного пользователя (ID 1)
        if (userId != null && userId.equals(1L)) {
            log.warn("UserService: deleteUser() -> updatedUser - User with ID 1 does not deleting!");
            throw new RuntimeException("ADMIN DELETE");
        }
        // Защита от самоудаления
        if(currentUser.equals(userRepository.findById(userId).orElseThrow(() -> new NotFoundUserException("Don't find user with id " + userId)))) {
            log.info("UserService: deleteUser -> You can not delete yourself!");
            throw new NoRightsException("You can not delete yourself!");
        }

        // Проверка наличия назначенных задач
        if(!userRepository.findById(userId).get().getAssignedTasks().isEmpty()) {
            log.info("UserService: deleteUser -> You can not delete user whene thera are tasks in him!");
            throw new HaveTasksException("You can not delete user whene thera are tasks in him!");
        }
        userRepository.deleteById(userId);
    }


    @Override
    public User updateUser(Long id, String username, String newPassword, String email, String fullName, Set<Role> roles, User currentUser) {

        if(!currentUser.getRoles().stream().anyMatch(Role.ADMINISTRATOR::equals)) {
            log.info("UserService: updateUser() -> user dont have permision to update user!");
            throw new NoRightsException("User dont have permision to update user!");
        }

        User dataBaseUser = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));

        // Защита системного пользователя
        if (id != null && id.equals(1L) || dataBaseUser.getUsername().equals("admin")) {
            log.warn("UserService: updateUser() -> updatedUser - User with ID 1 does not updating!");
            throw new RuntimeException("ADMIN UPDATE");
        }

        User sourceUser = User.builder()
                .username(username)
                .email(email)
                .fullName(fullName)
                .roles(roles)
                .build();

        // Использование BeanUtils для копирования свойств (кроме id и password)
        BeanUtils.copyProperties(sourceUser, dataBaseUser, "id", "password");

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(newPassword);
            dataBaseUser.setPassword(hashedPassword);
            log.info("UserService: updateUser() -> Password changed for User ID {}.", id);
        }

        log.info("UserService: updateUser() -> User with ID {} successfully updated by Administrator {}.",
                id, currentUser.getUsername());

        return userRepository.save(dataBaseUser);
    }


    @Override
    public User updateUserWithoutPassword(Long id, String username, String email, String fullName, Set<Role> roles, User currentUser) {

        if(!currentUser.getRoles().stream().anyMatch(Role.ADMINISTRATOR::equals)) {
            log.info("UserService: updateUser() -> user dont have permision to update user!");
            throw new NoRightsException("User dont have permision to update user!");
        }

        User dataBaseUser = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));


        // Защита системного пользователя
        if (id != null && id.equals(1L) || dataBaseUser.getUsername().equals("admin")) {
            log.warn("UserService: updateUser() -> updatedUser - User with ID 1 does not updating!");
            throw new RuntimeException("ADMIN UPDATE");
        }

        User sourceUser = User.builder()
                .username(username)
                .email(email)
                .fullName(fullName)
                .roles(roles)
                .build();

        // Использование BeanUtils для копирования свойств, сохраняя существующий пароль
        BeanUtils.copyProperties(sourceUser, dataBaseUser, "id", "password");

        log.info("UserService: updateUser() -> User with ID {} successfully updated by Administrator {}.",
                id, currentUser.getUsername());

        return userRepository.save(dataBaseUser);
    }



    private User createUserDatabase(String username, String password, String email, String fullName, Set<Role> roles){
        User user = User.builder()
                .username(username)
                // Обязательное хеширование пароля
                .password(passwordEncoder.encode(password))
                .email(email)
                .fullName(fullName)
                .roles(roles)
                .build();
        return userRepository.save(user);
    }



    @Transient
    public int getCountOfTasksWithСertainStatus(Long userId, TaskStatus status){
        User user =  userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("Не найден пользователь с ID: " + userId));
        return user.getAssignedTasks().stream().filter(task -> task.getStatus().equals(status)).toList().size();
    }

    @Transient
    public int getUserTasksWithHighPriority(Long userId){
        User user =  userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("Не найден пользователь с ID: " + userId));
        return user.getAssignedTasks().stream().filter(task ->
                task.getPriority().equals(Priority.URGENT) ||
                        task.getPriority().equals(Priority.HIGH)).toList().size();
    }
}