package com.main.taskmanager.user.service.react.impl;

import com.main.taskmanager.user.model.UserRole;
import com.main.taskmanager.user.model.enumclasses.Role;
import com.main.taskmanager.user.repository.ReactiveUserRolesRepository;
import com.main.taskmanager.user.service.react.ReactUserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class ReactUserRoleServiceImpl implements ReactUserRoleService {

    private final ReactiveUserRolesRepository reactiveUserRolesRepository;

    @Override
    public Mono<UserRole> create(Long userId, Role role) {
        return reactiveUserRolesRepository.save(new UserRole(userId, role)).doOnSuccess(v ->
                log.info("User created successfully for role {}. User id - {}", role, userId));
    }

    @Override
    public Mono<UserRole> updateRole(Long userId, Role role) {
        return reactiveUserRolesRepository.save(new UserRole(userId, role)).
                doOnSuccess(v -> log.info("User role updated successfully, user id {}", userId));
    }

    @Override
    public Mono<Void> deleteRole(Long userId) {
        return reactiveUserRolesRepository.findById(userId)
                .flatMap(userRole -> reactiveUserRolesRepository.delete(userRole))
                .doOnSuccess(v -> log.info("Role for user {} deleted", userId));
    }

    @Override
    public Flux<String> getRolesOfUserFlux(Long userId) {
        Set<Role> roles = new HashSet<>();
        return reactiveUserRolesRepository.findRolesOfUser(userId);
    }



}
