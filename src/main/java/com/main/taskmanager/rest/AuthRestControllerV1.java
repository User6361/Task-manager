package com.main.taskmanager.rest;

import com.main.taskmanager.blackList.model.BlackList;
import com.main.taskmanager.blackList.serv.BlackListServ;
import com.main.taskmanager.security.react.model.CustomPrincipal;
import com.main.taskmanager.security.react.service.SecurityService;
import com.main.taskmanager.token.serv.ReactTokenService;
import com.main.taskmanager.user.mapper.UserMapper;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.model.enumclasses.Role;
import com.main.taskmanager.user.service.react.impl.ReactUserRoleServiceImpl;
import com.main.taskmanager.user.service.react.impl.ReactUserServiceImlp;
import com.main.taskmanager.user.web.RegistrationRequest;
import com.main.taskmanager.user.web.UserResponse;
import com.main.taskmanager.web.model.AuthRequest;
import com.main.taskmanager.web.model.AuthResponse;
import com.main.taskmanager.web.model.RefreshRequest;
import com.main.taskmanager.web.model.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthRestControllerV1 {

    private final SecurityService securityService;
    private final ReactUserServiceImlp reactUserServiceImlp;
    private final UserMapper userMapper;
    private final ReactUserRoleServiceImpl reactUserRoleServiceImpl;
    private final ReactTokenService reactTokenService;
    private final BlackListServ blackListServ;

    @PostMapping("/register")
    public Mono<UserResponse> register(@RequestBody RegistrationRequest registrationRequest) {
        User user = userMapper.mapRegistr(registrationRequest);

        return reactUserServiceImlp.registerUser(user)
                .flatMap(savedUser -> {
                    return Flux.fromIterable(registrationRequest.getRoles())

                            .flatMap(role -> reactUserRoleServiceImpl.create(savedUser.getId(), role))
                            .then(Mono.just(savedUser));
                })
                .map(userMapper::map);
    }

    @PostMapping("/login")
    public Mono<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        return securityService.authenticate(authRequest.getUsername(), authRequest.getPassword());
    }

    @GetMapping("/info")
    public Mono<UserResponse> getUser(Authentication authentication) {
        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();
        Long userId = customPrincipal.getId();
        return reactUserServiceImlp.getUserById(userId)
                .flatMap(user ->
                        reactUserRoleServiceImpl.getRolesOfUserFlux(userId)
                                .map(Role::valueOf)
                                .collect(Collectors.toSet())
                                .map(rolesSet -> {
                                    user.setRoles(rolesSet);
                                    return user;
                                })
                )
                .map(userMapper::map);
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(Authentication authentication) {
        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();
        Long  userId = customPrincipal.getId();
        String token = (String) authentication.getCredentials();
        BlackList blackList = new BlackList().toBuilder()
                .expiryDate(LocalDateTime.now(ZoneId.of("UTC")))
                .token(token)
                .userId(customPrincipal.getId())
                .build();
        log.info("User id - " + userId);
        return reactTokenService.deleteByOwnerId(userId).then(blackListServ.save(blackList)).thenReturn(ResponseEntity.ok().build());
    }
    @PostMapping("/refresh")
    public Mono<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        return securityService.refresh(request.refreshToken());
    }

}
