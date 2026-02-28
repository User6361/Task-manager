package com.main.taskmanager.token.service;


import com.main.taskmanager.exception.RefreshTokeException;
import com.main.taskmanager.token.entity.RefreshToken;
import com.main.taskmanager.token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {


    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokeExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public Optional<RefreshToken> findByRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        var refreshToken = new RefreshToken().builder()
                .userId(userId)
                .expiryDate(Instant.now().plusMillis(refreshTokeExpiration.toMillis()))
                .token(UUID.randomUUID().toString())
                .build();
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }


    public RefreshToken checkRefreshToken(RefreshToken refreshToken) {
        if(refreshToken.getExpiryDate().compareTo(Instant.now()) < 0){
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokeException(refreshToken.getToken(), "Refresh token get expired. Signe in again");
        }

        return refreshToken;
    }


    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

}
