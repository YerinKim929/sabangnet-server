package com.daou.sabangnetserver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Tokens {

    @Id
    @Column(nullable = false)
    private int sellerNo;

    @Column(nullable = false, length = 512)
    private String accessToken;

    @Column(nullable = false, length = 512)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime refreshExpiresAt;
}
