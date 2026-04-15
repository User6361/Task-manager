package com.main.taskmanager.security.react.service;

import com.main.taskmanager.exception.AuthException;
import com.main.taskmanager.security.react.TokenDetails;
import com.main.taskmanager.token.enumclasses.TokenType;
import com.main.taskmanager.token.model.Token;
import com.main.taskmanager.token.serv.ReactTokenService;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.service.react.impl.ReactUserServiceImlp;
import com.main.taskmanager.web.model.AuthResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    private final ReactUserServiceImlp reactUserServiceImlp;
    private final PasswordEncoder passwordEncoder;
    private final ReactTokenService reactTokenService;

    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.expiration}")
    private Integer expirationInSeconds;
    @Value("${app.jwt.issuer}")
    private String issuer;
    @Value("${app.jwt.refreshExpiration}")
    private Integer refreshExpirationInSeconds;

    /// ACCESS TOKEN

    /// BEST_1
    private TokenDetails generateToken(User user) {

        Map<String, Object> claims = new HashMap<>(){{
            put("roles", user.getRoles());
            put("username",  user.getUsername());
            put("tokenType",  TokenType.ACCESS);
        }

        };
        return generateToken(claims, user.getId().toString());
    }
    /// BEST_2
    private TokenDetails generateToken( Map<String, Object> claims, String subject) {
        Long expirationTimeMillis = expirationInSeconds * 1000L;
        Date expirationDate = new Date(new Date().getTime() + expirationTimeMillis);

        return generateToken(expirationDate, claims, subject);
    }
    /// BEST_3
    private TokenDetails generateToken(Date expirationDate, Map<String, Object> claims, String subject) {
        
        ///ИЗМЕНЕНИЕ СПОСОБА ГЕНЕРАЦИИ ТОКЕНА 
        ///СТАРЫЙ МЕТОД БЫЛ ИЗМЕНЕН НА НОВЫЙ

        /// ИСПРАВЛЕНИЕ !!!!
        /// КЛЮЧ ПОДПИСИ ДЛЯ ТОКЕНА ТЕПЕРЬ ГЕНЕРИРУЕТСЯ В ФУНКЦИИ generateSingKey()
/*
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
*/
        Date createdDate = new Date();
        String token = Jwts.builder()
                .claims(claims)
                .issuer(issuer)
                .subject(subject)
                .issuedAt(createdDate)
                .id(UUID.randomUUID().toString())
                .expiration(expirationDate)
                .signWith(generateSingKey(), SignatureAlgorithm.HS256)
                .compact();

        return TokenDetails.builder()
                .token(token)
                .issuedAt(createdDate)
                .expiresAt(expirationDate)
                .tokenType(TokenType.ACCESS)
                .build();
    }



    /// REFRESH TOKEN


    /// BEST_1
    private TokenDetails generateRefreshToken(User user){
        Map<String, Object> claims = new HashMap<>(){{
            put("roles", user.getRoles());
            put("username",  user.getUsername());
            put("tokenType",  TokenType.REFRESH);
        }
        };
        return generateRefreshToken(claims, user.getId().toString());
    }
    /// BEST_2
    private TokenDetails generateRefreshToken(Map<String, Object> claims, String subject) {
        /// УСТАНАВЛИВАЕМ БОЛЬШОЙ ОТРЕЗОК ВРЕМЕНИ
        Long expirationTimeMillis = expirationInSeconds * 100000L;
        Date expirationDate = new Date(new Date().getTime() + expirationTimeMillis);

        return generateRefreshToken(expirationDate, claims, subject);
    }
    /// BEST_3
    private TokenDetails generateRefreshToken(Date expirationDate, Map<String, Object> claims, String subject) {
        Date createdDate = new Date();
        String token = Jwts.builder()
                .claims(claims)
                .issuer(issuer)
                .subject(subject)
                .issuedAt(createdDate)
                .id(UUID.randomUUID().toString())
                .expiration(expirationDate)
                .signWith(generateSingKey(), SignatureAlgorithm.HS256)
                .compact();

        return TokenDetails.builder()
                .token(token)
                .issuedAt(createdDate)
                .expiresAt(expirationDate)
                .tokenType(TokenType.REFRESH)
                .build();
    }


    public Mono<AuthResponse> authenticate(String username, String password) {
        return reactUserServiceImlp.getUserByUserName(username)
                .flatMap(user -> {

                    if(!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new AuthException("Incorrect password"));
                    }

                    TokenDetails refreshTokenDetails = generateRefreshToken(user);
                    TokenDetails accessTokenDetauls = generateToken(user);


                    Token refreshTokenForDB = Token.builder()
                            .token(refreshTokenDetails.getToken())
                            .expiredDate(LocalDateTime.ofInstant(refreshTokenDetails.getExpiresAt().toInstant(), ZoneId.systemDefault()))
                            .ownerId(user.getId())
                            .build();



                    log.info("Длина рефреш токена - {}", String.valueOf(refreshTokenDetails.getToken().toString().length()));
                    log.info("Длина аксес токена - {}",  String.valueOf(accessTokenDetauls.getToken().toString().length()));
                    return reactTokenService.save(refreshTokenForDB)
                            .map(savedToken -> AuthResponse.builder()
                                    .id(user.getId())
                                    .accessToken(accessTokenDetauls.getToken())
                                    .refreshToken(refreshTokenDetails.getToken())
                                    .issuedAt(accessTokenDetauls.getIssuedAt())
                                    .expiresAt(accessTokenDetauls.getExpiresAt())
                                    .message("Tokens generated and session saved")
                                    .build());
                })
                .switchIfEmpty(Mono.error(new AuthException(String.format("Не найден пользователь по данному username {}", username) )));
    }


    private Key generateSingKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
