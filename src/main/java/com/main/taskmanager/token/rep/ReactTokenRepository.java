package com.main.taskmanager.token.rep;

import com.main.taskmanager.token.model.Token;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ReactTokenRepository extends R2dbcRepository<Token, Long> {

    @Query("SELECT * FROM token WHERE owner_id = :userId")
    Mono<Token> findByUserId(Long userId);
    Mono<Void> deleteByOwnerId(Long ownerId);
    Mono<Token> findByToken(String token);
}
