package com.main.taskmanager.user.repository;

import com.main.taskmanager.user.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveUserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByUsername(String username);
}
