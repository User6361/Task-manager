package com.main.taskmanager.security;

import com.main.taskmanager.user.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Основной класс конфигурации безопасности приложения.
 * Аннотация {@code @EnableWebSecurity} включает поддержку безопасности Spring Security.
 * Определяет правила доступа к ресурсам, обработку аутентификации и механизм хеширования паролей.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Предоставляет бин для кодирования паролей.
     * Используется {@link BCryptPasswordEncoder} — надежный и рекомендованный алгоритм хеширования.
     * * @return Экземпляр {@link PasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Конфигурирует цепочку фильтров безопасности (Security Filter Chain),
     * определяющую правила авторизации и поведения аутентификации.
     *
     *
     * @param http Объект для настройки HTTP-безопасности.
     * @param userService Сервис для загрузки данных пользователя (реализует UserDetailsService).
     * @return Сконфигурированная цепочка фильтров безопасности.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UserService userService) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        // Разрешает доступ без аутентификации к корневой странице, страницам входа/регистрации и статическим ресурсам.
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**").permitAll()
                        // Требует аутентификации для всех остальных запросов.
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // Указывает URL страницы входа.
                        .loginPage("/login")
                        // URL перенаправления после успешного входа.
                        .defaultSuccessUrl("/tasks")
                        // URL перенаправления после неудачного входа.
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        // URL для выхода из системы (по умолчанию POST).
                        .logoutUrl("/logout")
                        // URL перенаправления после успешного выхода.
                        .logoutSuccessUrl("/login?logout=true")
                        // Инвалидирует HTTP-сессию.
                        .invalidateHttpSession(true)
                        // Удаляет куки JSESSIONID.
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // Указывает сервис для загрузки данных пользователя при аутентификации.
                .userDetailsService(userService);

        return http.build();
    }


}