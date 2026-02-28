package com.main.taskmanager.authorization;

import com.main.taskmanager.user.model.enumclasses.Role;
import com.main.taskmanager.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер для обработки запросов, связанных с аутентификацией и регистрацией пользователей.
 * Предоставляет страницы для входа (login) и регистрации (register), а также обрабатывает
 * POST-запросы для создания новых пользователей.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    // Сервис для выполнения операций, связанных с пользователями (создание, поиск).
    private final UserService userService;

    /**
     * Отображает страницу входа в систему.
     * Обрабатывает параметры запроса 'error' и 'logout', которые обычно передаются
     * Spring Security после неудачной попытки входа или успешного выхода из системы.
     *
     * @param error Флаг, указывающий на ошибку аутентификации.
     * @param logout Флаг, указывающий на успешный выход из системы.
     * @param model Модель для передачи сообщений об ошибках/успехе на Thymeleaf-шаблон.
     * @return Имя Thymeleaf-шаблона для страницы входа ("auth/login").
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "auth/login";
    }

    /**
     * Отображает страницу регистрации нового пользователя.
     *
     * @return Имя Thymeleaf-шаблона для страницы регистрации ("auth/register").
     */
//    @GetMapping("/register")
//    public String registerPage() {
//        return "auth/register";
//    }

    /**
     * Обрабатывает POST-запрос на регистрацию нового пользователя.
     * Вызывает метод сервисного слоя для создания пользователя, хеширования пароля и сохранения в БД.
     * В случае успеха перенаправляет на страницу входа, в случае ошибки — обратно на страницу регистрации.
     *
     * @param username Логин нового пользователя.
     * @param password Пароль (будет хеширован в сервисе).
     * @param email Email пользователя.
     * @param fullName Полное имя пользователя.
     * @param role Роль, назначенная новому пользователю (в виде строки, преобразуется в {@link Role}).
     * @param redirectAttributes Атрибуты для передачи flash-сообщений после перенаправления.
     * @return Перенаправление на страницу входа или регистрации.
     *
     * Пока данный метод не используется для данной программы
     *
     */
//    @PostMapping("/register")
//    public String registerUser(@RequestParam String username,
//                               @RequestParam String password,
//                               @RequestParam String email,
//                               @RequestParam String fullName,
//                               @RequestParam String role,
//                               RedirectAttributes redirectAttributes) {
//        try {
//            userService.createUserRegester(username, password, email, fullName, Role.valueOf(role));
//            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
//            return "redirect:/login";
//        } catch (Exception e) {
//            log.error("Registration error", e);
//            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
//            return "redirect:/register";
//        }
//    }

    /**
     * Корневой маппинг, перенаправляющий на основную страницу с задачами.
     *
     * @return Перенаправление на список задач ("/tasks").
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/tasks";
    }
}