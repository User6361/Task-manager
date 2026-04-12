package com.main.taskmanager.token.serv.impl;

import com.main.taskmanager.token.model.Token;
import com.main.taskmanager.token.rep.ReactTokenRepository;
import com.main.taskmanager.token.serv.ReactTokenService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class ReactTokenServiceImpl implements ReactTokenService {

    private final ReactTokenRepository reactTokenRepository;


    @Override
    public Mono<Token> save(Token token) {
        return reactTokenRepository.save(token);
    }

    @Override
    public Mono<Token> getToken(Long tokenId) {
        return reactTokenRepository.findById(tokenId);
    }

    @Override
    public Mono<Void> deleteToken(Long tokenId) {
        return reactTokenRepository.deleteById(tokenId);
    }

    @Override
    public Mono<Void> deleteAllTokens() {
        return reactTokenRepository.deleteAll();
    }

    @Override
    public Mono<Token> updateToken(Long tokenId, Token token) {
        return reactTokenRepository.findById(tokenId)
                .flatMap(existingToken -> {
                    existingToken.setToken(token.getToken());
                    existingToken.setExpired(token.isExpired());
                    existingToken.setRevoked(token.isRevoked());
                    existingToken.setOwnerId(token.getOwnerId());
                    return reactTokenRepository.save(existingToken);
                });
    }
}
