package com.main.taskmanager.web.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)

public class AuthResponse {
    private Long id;
    private String accessToken;
    private String refreshToken;
    private Date issuedAtAccessToken;
    private Date expiresAtAccessToken;
    private Date issuedAtRefreshToken;
    private Date expiresAtRefreshToken;
    private String message;
}
