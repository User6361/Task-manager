package com.main.taskmanager.web.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)

public class AuthResponse {
    private Long id;
    private String token;
    private Date issuedAt;
    private Date expiresAt;
}
