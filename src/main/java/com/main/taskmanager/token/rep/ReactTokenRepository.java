package com.main.taskmanager.token.rep;

import com.main.taskmanager.token.model.Token;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactTokenRepository extends R2dbcRepository<Token, Long> {
}
