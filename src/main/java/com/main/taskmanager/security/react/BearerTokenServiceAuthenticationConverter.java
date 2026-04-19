package com.main.taskmanager.security.react;

import com.main.taskmanager.jwt.handler.JwtHandler;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class BearerTokenServiceAuthenticationConverter implements ServerAuthenticationConverter {

    private final JwtHandler jwtHandler;
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.length() > BEARER_PREFIX.length())
                .filter(authHeader -> authHeader.toLowerCase().startsWith(BEARER_PREFIX.toLowerCase()))
                .map(authHeader -> authHeader.substring(BEARER_PREFIX.length()))
                .flatMap(jwtHandler::check)
                .flatMap(UserAuthenticationBearer::create);
    }
}
