package com.main.taskmanager.security.react;

import com.main.taskmanager.security.AppUserDetails;
import com.main.taskmanager.user.repository.ReactiveUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {
    private ReactiveUserRepository reactiveUserRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return  reactiveUserRepository.findByUsername(username).flatMap(
                user -> Mono.just(new AppUserDetails(user))
        );
    }
}
