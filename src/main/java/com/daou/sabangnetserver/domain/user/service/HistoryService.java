package com.daou.sabangnetserver.domain.user.service;

import com.daou.sabangnetserver.domain.user.dto.HistorySearchDto;
import com.daou.sabangnetserver.domain.user.dto.HistorySearchRequestDto;
import com.daou.sabangnetserver.domain.user.dto.HistorySearchResponseDto;
import com.daou.sabangnetserver.domain.user.entity.History;
import com.daou.sabangnetserver.domain.user.repository.HistoryRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepo;

    private HistorySearchDto toHistorySearchDto(History result){
        return HistorySearchDto.builder()
                .loginTime(result.getLoginTime())
                .name(result.getUser().getName())
                .id(result.getUser().getId())
                .loginDevice(result.getLoginDevice())
                .loginIp(result.getLoginIp())
                .build();
    }

    public HistorySearchResponseDto searchHistory(HistorySearchRequestDto requestDto){
        Pageable pageable = PageRequest.of(requestDto.getPage() - 1, requestDto.getShowList(), Sort.by("loginTime").descending());

        Page<History> historyPage = historyRepo.searchHistories(requestDto.getId(), requestDto.getName(), pageable);

        List<HistorySearchDto> historyDtos = historyPage.getContent().stream().map(this::toHistorySearchDto).collect(Collectors.toList());

        return HistorySearchResponseDto.builder()
                .totalLists((int) historyPage.getTotalElements())
                .histories(historyDtos)
                .totalPages(historyPage.getTotalPages())
                .build();
    }

}
