package com.main.taskmanager.security.react;

import com.main.taskmanager.jwt.handler.JwtHandler;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Set;

public class UserAuthenticationBearer {

    public static Mono<Authentication> create(JwtHandler.VerificationResult verificationResult) {
        Claims claims = verificationResult.claims;
        String subject = claims.getSubject();

        List<String> roles = claims.get("roles", List.class);
        String username = claims.get("username", String.class);


        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        Long principalId = Long.parseLong(subject);


        CustomPrincipal principal = new CustomPrincipal(principalId, username);

        return Mono.justOrEmpty(new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }
}
