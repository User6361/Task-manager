package com.main.taskmanager.blackList.serv;

import com.main.taskmanager.blackList.model.BlackList;
import reactor.core.publisher.Mono;

public interface BlackListServ {
    Mono<BlackList> findByUserId(Long userId);
    Mono<Void> deleteByUserId(Long userId);
    Mono<BlackList> save(BlackList blackList);
    Mono<Boolean> existsByToken(String token);
}
