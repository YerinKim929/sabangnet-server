package com.daou.sabangnetserver.domain.user.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSearchResponseDto {
    private int page;
    private int totalLists;
    private int totalPages;
    private List<UserDto> users;

    public static UserSearchResponseDto of(int page, int totalLists, int totalPages, List<UserDto> users) {
        return UserSearchResponseDto.builder()
                .page(page)
                .totalLists(totalLists)
                .totalPages(totalPages)
                .users(users)
                .build();
    }

}
