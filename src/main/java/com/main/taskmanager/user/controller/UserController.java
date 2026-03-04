package com.main.taskmanager.user.controller;

import com.main.taskmanager.aop.Loggable;
import com.main.taskmanager.exception.HaveTasksException;
import com.main.taskmanager.exception.NoRightsException;
import com.main.taskmanager.task.controller.TaskAssignFacade;
import com.main.taskmanager.task.model.Task;
import com.main.taskmanager.task.service.TaskService;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.model.enumclasses.Role;
import com.main.taskmanager.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Контроллер для обработки запросов, связанных с управлением пользователями.
 * Основные операции: просмотр списка пользователей, просмотр профиля, обновление данных (только Администратор) и удаление (только Администратор).
 */
@Controller
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private TaskAssignFacade taskAssignFacade;
    @Autowired
    private TaskService taskService;

    /**
     * Отображает список всех пользователей в системе.
     * Доступно только для Администратора.
     *
     * @param model Модель Spring UI.
     * @param currentUser Текущий авторизованный пользователь.
     * @return Имя Thymeleaf-шаблона для списка пользователей ("users/users").
     */
    @GetMapping
    @Loggable
    public String getAllUsers(Model model,@AuthenticationPrincipal User currentUser ) {
        List<Task> userTasks = taskService.getUserTasks(currentUser);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("countOfMyTasksMessage", taskAssignFacade.getCountOfMyTasksMessage(userTasks));
        return "users/users";
    }

    /**
     * Отображает детальный профиль конкретного пользователя.
     *
     * @param userId ID пользователя, чей профиль запрашивается.
     * @param model Модель Spring UI.
     * @param currentUser Текущий авторизованный пользователь.
     * @return Имя Thymeleaf-шаблона для просмотра профиля ("users/current-user") или перенаправление на "/users" в случае ошибки.
     */
    @GetMapping( value = "/{userId}")
    @Loggable
    public String getUser(@PathVariable("userId") Long userId, Model model, @AuthenticationPrincipal User currentUser) {

        Optional<User> userOptional = userService.getUserById(userId);

        if (userOptional.isEmpty()) {
            return "redirect:/users";
        }

        User userToView = userOptional.get();


        model.addAttribute("currentUser", currentUser);
        model.addAttribute("countOfMyTasksMessage",
                taskAssignFacade.getCountOfMyTasksMessage(taskService.getUserTasks(currentUser)));
        model.addAttribute("user_id", userToView.getId());
        model.addAttribute("full_name", userToView.getFullName());
        model.addAttribute("username", userToView.getUsername());
        model.addAttribute("email", userToView.getEmail());
        model.addAttribute("roles", userToView.getRoles());

        log.info("Viewing user profile for ID: {} by current user: {}", userId, currentUser.getUsername());
        /**Обязательно указывает начальную папку без / потому что потом может не сработать при переносе jar файла
         * потому что данный шаблон не будет виден**/
        return "users/current-user";
    }


    /**
     * Обрабатывает POST-запрос на обновление данных пользователя.
     * Единый метод для обновления с паролем или без.
     * Требует роли {@link Role#ADMINISTRATOR}.
     *
     * @param userId ID обновляемого пользователя.
     * @param username Новый логин.
     * @param password Новый пароль (может быть пустой строкой).
     * @param email Новый email.
     * @param fullName Новое полное имя.
     * @param role Новая роль.
     * @param currentUser Администратор, выполняющий операцию.
     * @param redirectAttributes Для передачи сообщений об успехе/ошибке после перенаправления.
     * @return Перенаправление на страницу обновленного пользователя.
     */
    @PostMapping(value = "/update/{userId}")
    public String updateUser(
            @PathVariable("userId") Long userId,
            @RequestParam String username,
            @RequestParam(required = false) String password, // Пароль может отсутствовать или быть пустым
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam Set<Role> roles,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes) {

        if (!currentUser.getRoles().stream().anyMatch(Role.ADMINISTRATOR::equals)) {
            log.warn("User {} attempted to update user {} without ADMIN rights.",
                    currentUser.getUsername(), userId);
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Only Administrators can update user profiles.");
            return "redirect:/users/" + userId;
        }

        try {
            // Выбор метода в зависимости от наличия пароля (логика делегируется сервису)
            if (password != null && !password.trim().isEmpty()) {
                userService.updateUser(userId, username, password, email, fullName, roles, currentUser);
                log.info("Administrator {} updated user {} data AND password.", currentUser.getUsername(), userId);
            } else {
                // Если пароль отсутствует или пустой, вызываем метод без обновления пароля
                userService.updateUserWithoutPassword(userId, username, email, fullName, roles, currentUser);
                log.info("Administrator {} updated user {} data (password untouched).", currentUser.getUsername(), userId);
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "User profile successfully updated!");

        } catch (Exception e) {

            log.error("Error updating user {} by Administrator {}: {}", userId, currentUser.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Update failed: " + e.getMessage());
        }


        return "redirect:/users/{userId}";
    }

    /**
     * Удаляет пользователя из системы.
     * Требует роли {@link Role#ADMINISTRATOR}.
     * <p>
     * Если у пользователя есть назначенные активные задачи, удаление блокируется, и происходит перенаправление обратно.
     *
     * @param userId ID удаляемого пользователя.
     * @param currentUser Администратор, выполняющий операцию.
     * @return Перенаправление на список пользователей ("/users") или обратно на профиль пользователя в случае ошибки (наличие задач).
     */
    @PostMapping(value = "/delete/{userId}")
    public String deleteUser(@PathVariable("userId") Long userId, @AuthenticationPrincipal User currentUser) {
        if(!currentUser.getRoles().stream().anyMatch(Role.ADMINISTRATOR::equals)) {
            log.info("Current user - " + userService.getUserById(userId).toString() + " can not delete user!");
            return "redirect:/users/{userId}";
        }
        try{
            userService.deleteUser(userId, currentUser);
        }catch(HaveTasksException e){
            // Если у пользователя есть активные задачи, возвращаемся на его профиль
            return "redirect:/users/{userId}";
        }
        return "redirect:/users";
    }



    // =======================================================
    // 1. GET: Отображение формы создания пользователя (ADMIN)
    // =======================================================
    /**
     * Отображает форму для создания нового пользователя.
     * Доступно только для {@link Role#ADMINISTRATOR}.
     *
     * @param model Модель Spring UI.
     * @param currentUser Текущий авторизованный пользователь.
     * @return Имя Thymeleaf-шаблона для формы создания пользователя.
     */
    @GetMapping("/create")
    public String getCreateUserForm(Model model, @AuthenticationPrincipal User currentUser) {

        // **АВТОРИЗАЦИЯ:** Строгая проверка прав
        if (!currentUser.getRoles().stream().anyMatch(Role.ADMINISTRATOR::equals)) {
            log.warn("User {} attempted to access the create user form without ADMIN rights.", currentUser.getUsername());
            throw new NoRightsException("Access denied. Only Administrators can create new users.");
        }

        // Передаем в модель список доступных ролей для выбора в форме
        List<String> roles = Arrays.stream(Role.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        model.addAttribute("roles", roles);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("countOfMyTasksMessage",
                taskAssignFacade.getCountOfMyTasksMessage(taskService.getUserTasks(currentUser)));

        log.info("Admin user {} accessing user creation form.", currentUser.getUsername());
        return "users/create-user"; // Название нового шаблона
    }

    // =======================================================
    // 2. POST: Обработка создания пользователя (ADMIN)
    // =======================================================
    /**
     * Обрабатывает POST-запрос на создание нового пользователя.
     * Доступно только для {@link Role#ADMINISTRATOR}.
     *
     * @param username Логин нового пользователя.
     * @param password Пароль.
     * @param email Email.
     * @param fullName Полное имя.
     * @param role Роль, назначенная Администратором.
     * @param currentUser Текущий авторизованный пользователь (Администратор).
     * @param redirectAttributes Для передачи сообщений после перенаправления.
     * @return Перенаправление на список пользователей или обратно на форму в случае ошибки.
     */
    @PostMapping("/create")
    public String createNewUser(@RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String email,
                                @RequestParam String fullName,
                                @RequestParam Set<Role> roles,
                                @AuthenticationPrincipal User currentUser,
                                RedirectAttributes redirectAttributes) {

        // **АВТОРИЗАЦИЯ:** Строгая проверка прав
        if (!currentUser.getRoles().stream().anyMatch(Role.ADMINISTRATOR::equals)) {
            log.warn("User {} attempted to create a user without ADMIN rights.", currentUser.getUsername());
            throw new NoRightsException("Access denied. Only Administrators can create new users.");
        }

        try {
            userService.createUser(username, password, email, fullName, roles, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + username + "' successfully created.");
            log.info("Administrator {} successfully created new user: {}",
                    currentUser.getUsername(), username);
            return "redirect:/users"; // Перенаправляем на список пользователей

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid role specified.");
            log.error("Invalid role value '{}' provided during user creation by {}", roles.toString(), currentUser.getUsername(), e);
            return "redirect:/users/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating user: " + e.getMessage());
            log.error("Error creating user by {}: {}", currentUser.getUsername(), e.getMessage(), e);
            return "redirect:/users/create";
        }
    }

}