package com.main.taskmanager.blackList.model;


import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("black_list")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@ToString
public class BlackList {
    private Long id;
    private String token;
    @Column("user_id")
    private Long userId;
    @Column("expiry_date")
    private LocalDateTime expiryDate;
}
