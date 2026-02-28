package com.main.taskmanager.user.service;

import com.main.taskmanager.task.model.Task;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.model.enumclasses.Role;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс сервисного слоя для управления сущностью {@link User}.
 * Расширяет {@link UserDetailsService}, обеспечивая интеграцию с механизмами
 * аутентификации Spring Security.
 */
public interface UserService extends UserDetailsService {

    /**
     * Возвращает список всех пользователей в системе.
     * @return Список всех {@link User}.
     */
    List<User> getAllUsers();

    /**
     * Находит пользователя по его уникальному ID.
     * @param id ID пользователя.
     * @return {@link Optional} с найденным пользователем.
     */
    Optional<User> getUserById(Long id);

    /**
     * Возвращает список всех задач, назначенных конкретному пользователю.
     * @param userId ID пользователя.
     * @return Список {@link Task}, назначенных пользователю.
     */
    List<Task> getAllTasksOfUser(Long userId);

    /**
     * Находит конкретную задачу, назначенную пользователю, по её ID.
     * (Примечание: обычно этот метод находится в TaskService).
     * @param id ID задачи.
     * @return {@link Optional} с найденной задачей.
     */
    Optional<Task> getUserTaskById(Long id);

    /**
     * Находит пользователя по его уникальному логину (username).
     * @param username Логин пользователя.
     * @return {@link Optional} с найденным пользователем.
     */
    Optional<User> getUserByUsername(String username);

    /**
     * Создает нового пользователя в системе (обычно используется для создания Администратором).
     * @param username Логин.
     * @param password Нехешированный пароль.
     * @param email Email.
     * @param fullName Полное имя.
     * @param role Роль пользователя.
     * @param currentUser Пользователь, выполняющий создание (для проверки прав).
     * @return Созданный {@link User}.
     */
    User createUser(String username, String password, String email, String fullName, Role role, User currentUser);

    /**
     * Создает пользователя во время инициализации приложения (например, через {@code CommandLineRunner}).
     * Не требует проверки прав, так как выполняется при старте системы.
     * @param username Логин.
     * @param password Нехешированный пароль.
     * @param email Email.
     * @param fullName Полное имя.
     * @param role Роль.
     * @return Созданный {@link User}.
     */
    User createUserInit(String username, String password, String email, String fullName, Role role);

    /**
     * Создает пользователя в процессе самостоятельной регистрации через веб-форму.
     * @param username Логин.
     * @param password Нехешированный пароль.
     * @param email Email.
     * @param fullName Полное имя.
     * @param role Роль (обычно {@code USER}).
     * @return Созданный {@link User}.
     */
    User createUserRegester(String username, String password, String email, String fullName, Role role);

    /**
     * Проверяет, существует ли пользователь с заданным логином.
     * @param username Логин для проверки.
     * @return {@code true}, если пользователь существует.
     */
    boolean userExists(String username);

    /**
     * Удаляет пользователя из системы.
     * Требует наличия прав {@code ADMINISTRATOR} и проверки, что у пользователя нет назначенных задач.
     * @param id ID удаляемого пользователя.
     * @param currentUser Пользователь, выполняющий удаление.
     */
    void deleteUser(Long id, User currentUser);

    /**
     * Обновляет данные пользователя, включая пароль (если он указан).
     * Требует наличия прав {@code ADMINISTRATOR}.
     * @param id ID обновляемого пользователя.
     * @param username Новый логин.
     * @param password Новый пароль.
     * @param email Новый email.
     * @param fullName Новое полное имя.
     * @param role Новая роль.
     * @param currentUser Пользователь, выполняющий обновление.
     * @return Обновленный {@link User}.
     */
    User updateUser(Long id, String username, String password, String email, String fullName, Role role, User currentUser);

    /**
     * Обновляет данные пользователя, сохраняя при этом текущий пароль.
     * Требует наличия прав {@code ADMINISTRATOR}.
     * @param id ID обновляемого пользователя.
     * @param username Новый логин.
     * @param email Новый email.
     * @param fullName Новое полное имя.
     * @param role Новая роль.
     * @param currentUser Пользователь, выполняющий обновление.
     * @return Обновленный {@link User}.
     */
    User updateUserWithoutPassword(Long id, String username,String email, String fullName, Role role, User currentUser);
}