package com.daou.sabangnetserver.domain.user.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class UserDeleteRequestDto {
    private List<String> ids;
}
