package com.main.taskmanager.token.serv;

import com.main.taskmanager.token.model.Token;
import reactor.core.publisher.Mono;

public interface ReactTokenService {
    Mono<Token> save(Token token);
    Mono<Token> getToken(Long tokenId);
    Mono<Void> deleteToken(Long tokenId);
    Mono<Void> deleteAllTokens();
    Mono<Token> updateToken(Long tokenId, Token token);

}
