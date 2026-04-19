package com.main.taskmanager.blackList.rep;

import com.main.taskmanager.blackList.model.BlackList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BlackListRep  extends R2dbcRepository<BlackList, Long> {
    Mono<BlackList> findByUserId(Long userId);
    Mono<Void> deleteByUserId(Long userId);
    Mono<Boolean> existsByToken(String token);
}
