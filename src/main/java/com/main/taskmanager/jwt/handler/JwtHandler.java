package com.main.taskmanager.jwt.handler;

import com.main.taskmanager.blackList.serv.BlackListServ;
import com.main.taskmanager.exception.AuthException;
import com.main.taskmanager.exception.ExpiredTokenException;
import com.main.taskmanager.exception.NotValidAccessTokenException;
import com.main.taskmanager.exception.UnauthorizedException;
import com.main.taskmanager.security.react.AuthenticationManager;
import com.main.taskmanager.token.enumclasses.TokenType;
import io.jsonwebtoken.Claims;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class JwtHandler {

    @Value("${app.jwt.secret}")
    private final String secret;
    @Autowired
    private BlackListServ blackListServ;

    public JwtHandler(@Value("${app.jwt.secret}") String secret) {
        this.secret = secret;
    }

    public Mono<VerificationResult> check(String accessToken){

        /// СМОТРИМ НЕ ОТПРАВИЛ ЛИ ПОЛЬЗОВАТЕЛЬ REFRESH ТОКЕН
        if(getTokenType(accessToken).equalsIgnoreCase("REFRESH")){
            return Mono.error(new RuntimeException("Refresh token must not be for access"));
        }
        return verify(accessToken).onErrorResume(e -> Mono.error(new UnauthorizedException(e.getMessage())));
    }

    public Mono<VerificationResult> checkRefreshToken(String refreshToken){
        if(!getTokenType(refreshToken).equalsIgnoreCase("REFRESH")){
            return Mono.error(new RuntimeException("ACCESS token must not be for refresh"));
        }
        return verify(refreshToken).onErrorResume(e -> Mono.error(new UnauthorizedException(e.getMessage())));
    }


    private Mono<VerificationResult> verify(String token) {
        return Mono.defer(() -> {
            try {
                Claims claims = getClaimsFromToken(token);
                final Date expirationDate = claims.getExpiration();
                String tokenType = String.valueOf(claims.get("tokenType"));
                if (expirationDate.before(new Date())) {
                    return Mono.error(new RuntimeException("Token has expired"));
                }
                return blackListServ.existsByToken(token)
                        .flatMap(isBlocked -> {
                            if (isBlocked) {
                                return Mono.error(new NotValidAccessTokenException("Token is not valid. Is on black list"));
                            }
                            return Mono.just(new VerificationResult(claims, token));
                        });

            } catch (Exception e) {
                return Mono.error(new RuntimeException("Token verification failed: " + e.getMessage()));
            }
        });
    }












    public String getTokenType(String token){
        Claims claims = getClaimsFromToken(token);
        final Date expirationDate = claims.getExpiration();
        String tokenType = String.valueOf(claims.get("tokenType"));
        return tokenType;
    }
    public Claims getClaimsFromToken(String token) {

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public static class VerificationResult{
        public Claims claims;
        public String token;

        public VerificationResult(Claims claims, String token){
            this.claims = claims;
            this.token = token;
        }

        public Claims claims() {
            return this.claims;
        }
    }
}
