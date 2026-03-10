package com.main.taskmanager.user.repository;

import com.main.taskmanager.user.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface ReactiveUserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByUsername(String username);
}
