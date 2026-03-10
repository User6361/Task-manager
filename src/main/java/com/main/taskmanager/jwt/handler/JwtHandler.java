package com.main.taskmanager.jwt.handler;

import com.main.taskmanager.exception.AuthException;
import com.main.taskmanager.exception.ExpiredTokenException;
import com.main.taskmanager.exception.UnahtorizedException;
import io.jsonwebtoken.Claims;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Date;

public class JwtHandler {

    private final String secret;

    public JwtHandler(String secret) {
        this.secret = secret;
    }

    public Mono<VerificationResult> check(String accessToken){
        return Mono.just( verify(accessToken)).onErrorResume(e -> Mono.error(new UnahtorizedException(e.getMessage())));
    }


    private VerificationResult verify(String token){
        Claims claims = getClaimsFromToken(token);
        final Date expirationDate = claims.getExpiration();
        if(expirationDate.before(new Date())){
             throw new RuntimeException("Token has expired");
        }
        return new VerificationResult(claims, token);

    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Base64.getEncoder().encode(secret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
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
