package com.daou.sabangnetserver.domain.user.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistorySearchDto {
    private LocalDateTime loginTime;
    private String name;
    private String id;
    private String loginDevice;
    private String loginIp;
}
