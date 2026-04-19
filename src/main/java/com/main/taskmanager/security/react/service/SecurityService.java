package com.main.taskmanager.security.react.service;

import com.main.taskmanager.blackList.model.BlackList;
import com.main.taskmanager.blackList.serv.BlackListServ;
import com.main.taskmanager.exception.AuthException;
import com.main.taskmanager.security.react.model.CustomPrincipal;
import com.main.taskmanager.security.react.model.TokenDetails;
import com.main.taskmanager.security.react.model.TokenDetailsAR;
import com.main.taskmanager.token.enumclasses.TokenType;
import com.main.taskmanager.token.model.Token;
import com.main.taskmanager.token.serv.ReactTokenService;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.service.react.ReactUserService;
import com.main.taskmanager.web.model.AuthResponse;
import com.main.taskmanager.web.model.TokenResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import com.main.taskmanager.jwt.handler.JwtHandler;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    private final ReactUserService reactUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtHandler jwtHandler;
    private final ReactTokenService reactTokenService;
    private final BlackListServ blackListServ;
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
        Long expirationTimeMillis = expirationInSeconds + 1000L;
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
        Long expirationTimeMillis = refreshExpirationInSeconds + 1000L;
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
        return reactUserService.getUserByUserName(username)
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
                                    .issuedAtAccessToken(accessTokenDetauls.getIssuedAt())
                                    .expiresAtAccessToken(accessTokenDetauls.getExpiresAt())
                                    .issuedAtRefreshToken(refreshTokenDetails.getIssuedAt())
                                    .expiresAtRefreshToken(refreshTokenDetails.getExpiresAt())
                                    .message("Tokens generated and session saved")
                                    .build());
                })
                .switchIfEmpty(Mono.error(new AuthException(String.format("Не найден пользователь по данному username {}", username) )));
    }

    public Mono<ResponseEntity<Void>> logout(Authentication authentication) {
        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();
        String token = authentication.getCredentials().toString();
        BlackList blackListEntry = BlackList.builder()
                .token(token)
                .userId(customPrincipal.getId())
                .expiryDate(LocalDateTime.now())
                .build();


        return reactTokenService.deleteByOwnerId(customPrincipal.getId())
                .then(blackListServ.save(blackListEntry))
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    public Mono<AuthResponse> refresh(String refreshToken) {
        return jwtHandler.checkRefreshToken(refreshToken)
                .flatMap(verificationResult -> {
                    Long userId = Long.parseLong(verificationResult.claims().getSubject());

                    return reactTokenService.findByToken(refreshToken)
                            .switchIfEmpty(Mono.error(new AuthException("Token not found")))
                            .flatMap(oldToken -> reactUserService.getUserById(userId)
                                    .flatMap(user -> {
                                        TokenDetailsAR tokenDetailsAR = generateARTokens(user);

                                        AuthResponse response = new AuthResponse().toBuilder()
                                                .id(user.getId())
                                                .accessToken(tokenDetailsAR.accessToken().getToken())
                                                .refreshToken(tokenDetailsAR.refreshToken().getToken())
                                                .issuedAtAccessToken(tokenDetailsAR.accessToken().getIssuedAt())
                                                .expiresAtAccessToken(tokenDetailsAR.accessToken().getExpiresAt())
                                                .issuedAtRefreshToken(tokenDetailsAR.refreshToken().getIssuedAt())
                                                .expiresAtRefreshToken(tokenDetailsAR.refreshToken().getExpiresAt())
                                                .message("Tokens generated and session saved")
                                                .build();

                                        Token refreshTokenForDB = Token.builder()
                                                .token(tokenDetailsAR.refreshToken().getToken())
                                                .expiredDate(tokenDetailsAR.refreshToken().getExpiresAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                                                .ownerId(user.getId())
                                                .build();

                                        return reactTokenService.save(refreshTokenForDB)
                                                .thenReturn(response);
                                    }));
                });
    }


    private TokenDetailsAR generateARTokens(User user){
        TokenDetails accessToken = generateToken(user);
        TokenDetails refreshToken = generateRefreshToken(user);
        return new TokenDetailsAR(accessToken, refreshToken);

    }


    private Key generateSingKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
