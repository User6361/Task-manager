package com.main.taskmanager.user.repository;

import com.main.taskmanager.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Репозиторий для доступа к данным сущности {@link User}.
 * Расширяет {@link JpaRepository}, предоставляя стандартные CRUD-операции.
 * Содержит кастомные методы (Derived Query Methods), критически важные для
 * аутентификации в Spring Security.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по его уникальному логину (username).
     * Этот метод необходим для реализации {@code UserDetailsService} в Spring Security.
     *
     * <p>Эквивалентно JPQL: {@code SELECT u FROM User u WHERE u.username = :username}</p>
     *
     * @param username Логин пользователя.
     * @return {@link Optional} с найденным пользователем или пустым значением.
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверяет, существует ли пользователь с заданным логином в базе данных.
     * Используется при регистрации для обеспечения уникальности логина.
     *
     * @param username Логин пользователя.
     * @return {@code true}, если пользователь с таким логином существует, иначе {@code false}.
     */
    Boolean existsByUsername(String username);


    Boolean existsByEmail(String email);
}