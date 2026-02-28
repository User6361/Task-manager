package com.main.taskmanager.user.service.impl;

import com.main.taskmanager.aop.Loggable;
import com.main.taskmanager.exception.HaveTasksException;
import com.main.taskmanager.exception.NoRightsException;
import com.main.taskmanager.exception.NotFoundUserException;
import com.main.taskmanager.task.model.Task;
import com.main.taskmanager.task.repository.TaskRepository;
import com.main.taskmanager.user.model.enumclasses.Role;
import com.main.taskmanager.user.service.UserService;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.repository.UserRepository;
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
    // NOTE: TaskRepository не отмечен final, что может указывать на инъекцию через сеттер или ошибку.
    private TaskRepository taskRepository;

    /**
     * @inheritDoc
     */
    @Override
    @Loggable
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * @inheritDoc
     */
    @Override
    @Loggable
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * @inheritDoc
     */
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

    /**
     * @inheritDoc
     */
    @Override
    public Optional<Task> getUserTaskById(Long taskId) {
        if(taskRepository.findById(taskId).isPresent()) {
            return taskRepository.findById(taskId);
        }
        log.info("UserService: getUserTaskById -> no task with id - " + taskId);
        return Optional.empty();
    }

    /**
     * @inheritDoc
     */
    @Override
    @Loggable
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * @inheritDoc
     * <p>
     * <b>Требует роли: {@link Role#ADMINISTRATOR}</b>.
     * </p>
     * @throws NoRightsException Если текущий пользователь не Администратор.
     */
    @Override
    @Transactional
    @Loggable
    public User createUser(String username, String password, String email, String fullName, Role role, User currentUser) {
        if(!currentUser.getRole().equals(Role.ADMINISTRATOR)) {
            log.info("UserService: createUser() -> user dont have permision to create user!");
            throw new NoRightsException("User dont have permision to create user!");
        }
        return createUserDatabase(username, password, email, fullName, role);
    }

    /**
     * @inheritDoc (Используется для инициализации, без проверки прав).
     */
    @Override
    @Transactional
    @Loggable
    public User createUserInit(String username, String password, String email, String fullName, Role role) {
        return createUserDatabase(username, password, email, fullName, role);
    }

    /**
     * @inheritDoc (Используется для самостоятельной регистрации).
     */
    @Override
    @Transactional
    @Loggable
    public User createUserRegester(String username, String password, String email, String fullName, Role role) {
        return createUserDatabase(username, password, email, fullName, role);
    }


    /**
     * @inheritDoc
     */
    @Override
    @Loggable
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }


    /**
     * Метод, требуемый интерфейсом {@link UserDetailsService}.
     * Используется Spring Security для загрузки данных пользователя во время аутентификации.
     *
     * @param username Логин пользователя.
     * @return Объект {@link UserDetails} (которым является {@link User}).
     * @throws UsernameNotFoundException Если пользователь не найден.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Удаляет пользователя из системы.
     * <p>
     * <b>Требует роли: {@link Role#ADMINISTRATOR}</b>.
     * </p>
     *
     * @param userId ID удаляемого пользователя.
     * @param currentUser Пользователь, выполняющий удаление.
     * @throws UsernameNotFoundException Если удаляемый пользователь не найден.
     * @throws NoRightsException Если текущий пользователь не Администратор или пытается удалить самого себя.
     * @throws HaveTasksException Если у пользователя есть назначенные задачи.
     */
    @Override
    public void deleteUser(Long userId, User currentUser){
        if(!userRepository.findById(userId).isPresent()){
            throw new UsernameNotFoundException("User not found: " + userId);
        }
        if(!currentUser.getRole().equals(Role.ADMINISTRATOR)) {
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


    /**
     * Обновляет данные пользователя, включая хеширование нового пароля, если он предоставлен.
     * <p>
     * <b>Требует роли: {@link Role#ADMINISTRATOR}</b>.
     * </p>
     *
     * @throws NoRightsException Если текущий пользователь не Администратор.
     * @throws UsernameNotFoundException Если обновляемый пользователь не найден.
     */
    @Override
    public User updateUser(Long id, String username, String newPassword, String email, String fullName, Role role, User currentUser) {

        if(!currentUser.getRole().equals(Role.ADMINISTRATOR)) {
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
                .role(role)
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


    /**
     * Обновляет данные пользователя без изменения пароля.
     * Используется, если Администратор не вводил новый пароль в форме.
     * <p>
     * <b>Требует роли: {@link Role#ADMINISTRATOR}</b>.
     * </p>
     *
     * @throws NoRightsException Если текущий пользователь не Администратор.
     * @throws UsernameNotFoundException Если обновляемый пользователь не найден.
     */
    @Override
    public User updateUserWithoutPassword(Long id, String username, String email, String fullName, Role role, User currentUser) {

        if(!currentUser.getRole().equals(Role.ADMINISTRATOR)) {
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
                .role(role)
                .build();

        // Использование BeanUtils для копирования свойств, сохраняя существующий пароль
        BeanUtils.copyProperties(sourceUser, dataBaseUser, "id", "password");

        log.info("UserService: updateUser() -> User with ID {} successfully updated by Administrator {}.",
                id, currentUser.getUsername());

        return userRepository.save(dataBaseUser);
    }


    /**
     * Вспомогательный метод для создания и хеширования пароля пользователя перед сохранением.
     *
     * @param username Логин.
     * @param password Нехешированный пароль.
     * @param email Email.
     * @param fullName Полное имя.
     * @param role Роль.
     * @return Созданная сущность {@link User} (еще не сохранена, если не используется {@code userRepository.save(user)} внутри).
     */
    private User createUserDatabase(String username, String password, String email, String fullName, Role role){
        User user = User.builder()
                .username(username)
                // Обязательное хеширование пароля
                .password(passwordEncoder.encode(password))
                .email(email)
                .fullName(fullName)
                .role(role)
                .build();
        return userRepository.save(user);
    }
}