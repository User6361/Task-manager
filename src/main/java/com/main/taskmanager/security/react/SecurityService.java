package com.main.taskmanager.security.react;

import com.main.taskmanager.exception.AuthException;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.repository.ReactiveUserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
@RequiredArgsConstructor
public class SecurityService {

    private final ReactiveUserRepository reactiveUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.expiration}")
    private Integer expirationInSeconds;
    @Value("${app.jwt.issuer}")
    private String issuer;


    private TokenDetails generateToken(User user) {

        Map<String, Object> claims = new HashMap<>(){{
            put("roles", user.getRoles());
            put("username",  user.getUsername());
        }
        };
        return generateToken(claims, user.getId().toString());
    }

    public TokenDetails generateToken( Map<String, Object> claims, String subject) {
        Long expirationTimeMillis = expirationInSeconds * 1000L;
        Date expirationDate = new Date(new Date().getTime() + expirationTimeMillis);

        return generateToken(expirationDate, claims, subject);
    }

    private TokenDetails generateToken(Date expirationDate, Map<String, Object> claims, String subject) {
        Date createdDate = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setId(UUID.randomUUID().toString())
                .setExpiration(createdDate)
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secret.getBytes()))
                .compact();

        return TokenDetails.builder()
                .token(token)
                .issuedAt(createdDate)
                .expiresAt(expirationDate)
                .build();
    }

    public Mono<TokenDetails> authenticate(String username, String password) {
        return reactiveUserRepository.findByUsername(username)
                .flatMap(user -> {
                    if(passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new AuthException("Incorrect password"));
                    }
                    return Mono.just(generateToken(user).toBuilder().id(user.getId()).build());
                })
                .switchIfEmpty(Mono.error(new AuthException(String.format("Не найден пользователь по данному username {}", username) )));
    }

}
