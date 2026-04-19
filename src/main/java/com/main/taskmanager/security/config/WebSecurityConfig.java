package com.main.taskmanager.security.config;

import com.main.taskmanager.jwt.handler.JwtHandler;
import com.main.taskmanager.security.react.AuthenticationManager;
import com.main.taskmanager.security.react.BearerTokenServiceAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanRegistrarDslMarker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "auth", havingValue = "token")
@Slf4j
public class WebSecurityConfig {


    @Value("${app.jwt.secret}")
    private String secret;
    @Autowired
    private JwtHandler jwtHandler;
    private final String[] publicRoutes = {"/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh"};


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, AuthenticationManager authenticationManager) {
        return http
                .csrf(csrf -> csrf.disable())

                .authorizeExchange(ex ->
                       ex
                               .pathMatchers(publicRoutes)
                               .permitAll()
                               .anyExchange()
                               .authenticated()
                )


                .addFilterAt(bearerAuthFilter(authenticationManager), SecurityWebFiltersOrder.AUTHORIZATION )
                .exceptionHandling(exceptionHandlingSpec ->
                {
                    log.error("Authentication Failed. Called in Security filter chain: {}");
                    exceptionHandlingSpec.authenticationEntryPoint((swe, e) -> {
                        log.error("UNAUTHORIZED! {}", e.getMessage());
                        return Mono.fromRunnable(() -> {swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);});
                    });
                    exceptionHandlingSpec.accessDeniedHandler((swe, e) -> {
                        log.error("FORBIDDEN! {}", e.getMessage());
                        return Mono.fromRunnable(() -> {swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);});
                    });
                })
                .authenticationManager(authenticationManager)
                .build();

    }

    private AuthenticationWebFilter bearerAuthFilter(AuthenticationManager authenticationManager) {
        AuthenticationWebFilter bearerAuthFilter = new AuthenticationWebFilter(authenticationManager);
        bearerAuthFilter.setServerAuthenticationConverter(new BearerTokenServiceAuthenticationConverter(jwtHandler));
        bearerAuthFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));
        return bearerAuthFilter;

    }

}
