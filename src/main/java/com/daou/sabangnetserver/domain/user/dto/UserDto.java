package com.daou.sabangnetserver.domain.user.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long userId;
    private String authority;
    private String id;
    private String name;
    private String email;
    private String department;
    private String memo;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Boolean isUsed;
}
