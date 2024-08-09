package com.daou.sabangnetserver.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSearchRequestDto {
    private String id;
    private String name;
    private String email;
    private Boolean isUsed;
    private int page;
    private int showList;
}
