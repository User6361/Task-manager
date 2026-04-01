package com.main.taskmanager.user.repository;


import com.main.taskmanager.user.model.UserRole;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface ReactiveUserRolesRepository extends R2dbcRepository<UserRole, Long> {

}
