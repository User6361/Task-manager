package com.main.taskmanager.rest;

import com.main.taskmanager.security.react.CustomPrincipal;
import com.main.taskmanager.security.react.service.SecurityService;
import com.main.taskmanager.user.mapper.UserMapper;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.service.react.impl.ReactUserServiceImlp;
import com.main.taskmanager.user.web.RegistrationRequest;
import com.main.taskmanager.user.web.UserResponse;
import com.main.taskmanager.web.model.AuthRequest;
import com.main.taskmanager.web.model.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRestControllerV1 {

    private final SecurityService securityService;
    private final ReactUserServiceImlp reactUserServiceImlp;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public Mono<UserResponse> register(@RequestBody RegistrationRequest registrationRequest) {
        User user = userMapper.mapRegistr(registrationRequest);
        return reactUserServiceImlp.registerUser(user).map(userMapper::map);
    }

    @PostMapping("/login")
    public Mono<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        return securityService.authenticate(authRequest.getUsername(), authRequest.getPassword()).flatMap(
                tokenDetails -> Mono.just(
                        AuthResponse.builder()
                                .id(tokenDetails.getId())
                                .token(tokenDetails.getToken())
                                .issuedAt(tokenDetails.getIssuedAt())
                                .expiresAt(tokenDetails.getExpiresAt())
                                .build()
                ));
    }

    @GetMapping("/info")
    public Mono<UserResponse> getUser(Authentication authentication) {
        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();
        return reactUserServiceImlp.getUserById(customPrincipal.getId()).
                map(userMapper::map);
    }

}
