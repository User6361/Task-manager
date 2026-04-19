package com.main.taskmanager.user.service.react.impl;

import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.model.enumclasses.Role;
import com.main.taskmanager.user.repository.ReactiveUserRepository;
import com.main.taskmanager.user.service.react.ReactUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactUserServiceImlp implements ReactUserService {
    private final ReactiveUserRepository reactiveUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> registerUser(User user) {
        return reactiveUserRepository.save(
                user.toBuilder()
                        .password(passwordEncoder.encode(user.getPassword()))
                        .roles(user.getRoles())

                        .build()
        ).doOnSuccess(u -> {
            log.info("Register user {}", u);
        });
    }

    @Override
    public Mono<User> getUserById(Long id) {
        return reactiveUserRepository.findById(id);
    }

    @Override
    public Mono<User> getUserByUserName(String username) {
        return reactiveUserRepository.findByUsername(username);
    }


}
