package com.main.taskmanager.init;


import com.main.taskmanager.user.model.enumclasses.Role;
import com.main.taskmanager.user.service.UserService;
import com.main.taskmanager.user.service.react.ReactUserService;
import com.main.taskmanager.user.service.react.impl.ReactUserServiceImlp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


/**
 * Компонент инициализации данных, реализующий интерфейс {@link CommandLineRunner}.
 * Обеспечивает создание критически важных и тестовых пользователей в базе данных
 * при первом запуске Spring Boot приложения.
 *
 * Создает системного пользователя с ролью ADMINISTRATOR и нескольких тестовых пользователей.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ReactUserService userService;

    /**
     * Метод, выполняемый после старта контекста Spring.
     * Проверяет существование системного и тестовых пользователей и создает их,
     * если они отсутствуют в базе данных.
     *
     */
    @Override
    public void run(String... args) throws Exception {
        // Инициализация пользователя администратора
        if (!userService.userExists("admin")) {
            userService.createUserInit("admin", "admin", "admin@taskmanager.com", "Administrator", Role.ADMINISTRATOR);
            log.info("Created admin user");
        }

        // Инициализация дефолтных пользователей для тестирования
        if (!userService.userExists("user1")) {
            userService.createUserInit("user1", "password", "user1@taskmanager.com", "John Doe", Role.USER);
            log.info("Created user1");
        }
        if (!userService.userExists("user2")) {
            userService.createUserInit("user2", "password", "user2@taskmanager.com", "Jane Smith", Role.USER);
            log.info("Created user2");
        }
    }
}