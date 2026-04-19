package com.main.taskmanager.security.react.model;

import com.main.taskmanager.token.enumclasses.TokenType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TokenDetails {
    private String token;
    private Date issuedAt;
    private Date expiresAt;
    private TokenType tokenType;
    private boolean revoked;
}
