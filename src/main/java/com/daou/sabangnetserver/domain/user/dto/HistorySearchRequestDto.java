package com.daou.sabangnetserver.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistorySearchRequestDto {
    private String id;
    private String name;
    private int page;
    private int showList;
}
