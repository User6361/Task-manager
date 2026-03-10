package com.main.taskmanager.security.react;

import com.main.taskmanager.user.repository.ReactiveUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final ReactiveUserRepository reactiveUserRepository;
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        return reactiveUserRepository.findById(principal.getId())
                .map(user -> authentication);


    }
}
