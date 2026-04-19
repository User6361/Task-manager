package com.main.taskmanager.user.repository;


import com.main.taskmanager.user.model.UserRole;
import com.main.taskmanager.user.model.enumclasses.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface ReactiveUserRolesRepository extends R2dbcRepository<UserRole, Long> {
    @Query("SELECT role_name FROM user_roles AS ur WHERE ur.user_id = :userId")
    Flux<String> findRolesOfUser(Long userId);

}
