package com.main.taskmanager.user.mapper;

import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.web.UserResponse;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse map(User user);
    @InheritInverseConfiguration
    User map(UserResponse userResponse);

}
