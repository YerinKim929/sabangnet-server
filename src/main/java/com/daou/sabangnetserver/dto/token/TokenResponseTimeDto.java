package com.daou.sabangnetserver.dto.token;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseTimeDto {
    private LocalDateTime expiresAt;
    private LocalDateTime refreshExpiresAt;
}