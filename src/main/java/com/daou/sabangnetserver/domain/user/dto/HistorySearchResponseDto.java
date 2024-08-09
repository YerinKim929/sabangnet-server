package com.daou.sabangnetserver.domain.user.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistorySearchResponseDto {
    private int totalLists;
    private int totalPages;
    private List<HistorySearchDto> histories;
}
