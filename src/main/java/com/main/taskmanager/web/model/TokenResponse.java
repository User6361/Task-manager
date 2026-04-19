package com.main.taskmanager.web.model;

import io.soabase.recordbuilder.core.RecordBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Builder
@RecordBuilder
public record TokenResponse(
        String accessToken,
        String refreshToken,
        Date accessExpiry,
        Date refreshExpiry
) {
}
