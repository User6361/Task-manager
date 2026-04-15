package com.main.taskmanager.jwt.handler;

import com.main.taskmanager.exception.AuthException;
import com.main.taskmanager.exception.ExpiredTokenException;
import com.main.taskmanager.exception.UnahtorizedException;
import com.main.taskmanager.security.react.AuthenticationManager;
import io.jsonwebtoken.Claims;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
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

    private final String secret;

    public JwtHandler(@Value("${app.jwt.secret}") String secret) {
        this.secret = secret;
    }

    public Mono<VerificationResult> check(String accessToken){
        return Mono.just( verify(accessToken)).onErrorResume(e -> Mono.error(new UnahtorizedException(e.getMessage())));
    }



    private VerificationResult verify(String token){
        Claims claims = getClaimsFromToken(token);
        final Date expirationDate = claims.getExpiration();
        String tokenType = claims.get("tokenType").toString();
        if(expirationDate.before(new Date())){
             throw new RuntimeException("Token has expired");
        }
        if(tokenType.equals("REFRESH")){
            throw new RuntimeException("Refresh token must not to be for access");
        }
        return new VerificationResult(claims, token);

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
    }
}
