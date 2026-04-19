package com.main.taskmanager.blackList.serv.impl;

import com.main.taskmanager.blackList.model.BlackList;
import com.main.taskmanager.blackList.rep.BlackListRep;
import com.main.taskmanager.blackList.serv.BlackListServ;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BlackListServImpl implements BlackListServ {
    private final BlackListRep  blackListRep;

    @Override
    public Mono<BlackList> findByUserId(Long userId) {
        return blackListRep.findByUserId(userId);
    }

    @Override
    public Mono<Void> deleteByUserId(Long userId) {
        return blackListRep.deleteByUserId(userId);
    }

    @Override
    public Mono<BlackList> save(BlackList blackList) {
        return blackListRep.save(blackList);
    }

    @Override
    public Mono<Boolean> existsByToken(String token) {
        return blackListRep.existsByToken(token);
    }
}
