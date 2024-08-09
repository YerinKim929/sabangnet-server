package com.daou.sabangnetserver.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class AuthResponseDto {
    private String id;
    private String authority;
}
