package com.main.taskmanager.user.model;

import com.main.taskmanager.user.model.enumclasses.Role;
import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    @Column("user_id")
    private Long userId;

    @Column("role_name")
    private Role role;
}