package com.main.taskmanager.security.react.service;

import com.main.taskmanager.exception.AuthException;
import com.main.taskmanager.security.react.TokenDetails;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.service.react.impl.ReactUserServiceImlp;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    private final ReactUserServiceImlp reactUserServiceImlp;
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
        
        ///ИЗМЕНЕНИЕ СПОСОБА ГЕНЕРАЦИИ ТОКЕНА 
        ///СТАРЫЙ МЕТОД БЫЛ ИЗМЕНЕН НА НОВЫЙ
        
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date createdDate = new Date();
        String token = Jwts.builder()
                .claims(claims)
                .issuer(issuer)
                .subject(subject)
                .issuedAt(createdDate)
                .id(UUID.randomUUID().toString())
                .expiration(expirationDate)
                .signWith(key)
                .compact();

        return TokenDetails.builder()
                .token(token)
                .issuedAt(createdDate)
                .expiresAt(expirationDate)
                .build();
    }

    public Mono<TokenDetails> authenticate(String username, String password) {
        return reactUserServiceImlp.getUserByUserName(username)
                .flatMap(user -> {

                    if(!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new AuthException("Incorrect password"));
                    }
                    return Mono.just(generateToken(user).toBuilder().id(user.getId()).build());
                })
                .switchIfEmpty(Mono.error(new AuthException(String.format("Не найден пользователь по данному username {}", username) )));
    }

}
