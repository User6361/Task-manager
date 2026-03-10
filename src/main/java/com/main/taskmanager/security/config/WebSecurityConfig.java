package com.main.taskmanager.security.config;

import com.main.taskmanager.jwt.handler.JwtHandler;
import com.main.taskmanager.security.react.AuthenticationManager;
import com.main.taskmanager.security.react.BearerTokenServiceAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "auth", havingValue = "token")
public class WebSecurityConfig {


    @Value("${app.jwt.secret}")
    private final String secret;
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, AuthenticationManager authenticationManager) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(authorizeExchangeSpec ->  authorizeExchangeSpec.anyExchange().authenticated())
                .addFilterAt(bearerAuthFilter(authenticationManager), SecurityWebFiltersOrder.AUTHORIZATION )
                .exceptionHandling()
                .build();

    }

    private AuthenticationWebFilter bearerAuthFilter(AuthenticationManager authenticationManager) {
        AuthenticationWebFilter bearerAuthFilter = new AuthenticationWebFilter(authenticationManager);
        bearerAuthFilter.setServerAuthenticationConverter(new BearerTokenServiceAuthenticationConverter(new JwtHandler(secret)));
        bearerAuthFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));
        return bearerAuthFilter;

    }

}
