package com.main.taskmanager.user.service.react;

import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.model.enumclasses.Role;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface ReactUserService {
    public Mono<User> registerUser(User user);
    public Mono<User> getUserById(Long id);
    public Mono<User> getUserByUserName(String username);
}
