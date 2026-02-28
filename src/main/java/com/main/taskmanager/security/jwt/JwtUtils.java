package com.main.taskmanager.security.jwt;

import com.main.taskmanager.security.AppUserDetails;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;


@Component
@Slf4j

public class JwtUtils {


    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.tokenExpiration}")
    private Duration tokenExpiration;


    public String generateJwtToken(AppUserDetails userDetails){
        return generateTokenFromUsername(userDetails.getUsername());
    }

    public String generateTokenFromUsername(String username) {

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime()  + tokenExpiration.toMillis()))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }



    public String getUsername(String token){
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }


    public boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        }catch(MalformedJwtException ex){
            log.error("Invalid JWT token: {}", ex.getMessage());
        }catch(ExpiredJwtException ex){
            log.error("Expired JWT token: {}", ex.getMessage());
        }catch(UnsupportedJwtException ex){
            log.error("Unsupported JWT token: {}", ex.getMessage());
        }catch(IllegalArgumentException ex){
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
}


