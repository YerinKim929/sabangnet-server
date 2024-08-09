package com.daou.sabangnetserver.domain.auth.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginServiceDto {
    private String id;
    private String password;
    private String loginDevice;
    private String loginIp;
    private LocalDateTime loginTime;
}
