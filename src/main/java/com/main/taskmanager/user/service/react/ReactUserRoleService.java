package com.main.taskmanager.user.service.react;

import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.model.UserRole;
import com.main.taskmanager.user.model.enumclasses.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface ReactUserRoleService {
    Mono<UserRole> create(Long userId, Role role);
    Mono<UserRole> updateRole(Long userId, Role role);
    Mono<Void> deleteRole(Long userId);
    Flux<String> getRolesOfUserFlux(Long userId);
}
