package com.main.taskmanager.web.model;


import com.main.taskmanager.user.model.enumclasses.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest {
    private String username;
    private String email;
    private Set<Role> roles;
    private String password;

}
