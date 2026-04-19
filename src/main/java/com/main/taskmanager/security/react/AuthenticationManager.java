package com.main.taskmanager.security.react;

import com.main.taskmanager.security.react.model.CustomPrincipal;
import com.main.taskmanager.user.service.react.impl.ReactUserServiceImlp;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final ReactUserServiceImlp reactUserServiceImlp;
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        return reactUserServiceImlp.getUserById(principal.getId())
                .map(user -> authentication);


    }
}
