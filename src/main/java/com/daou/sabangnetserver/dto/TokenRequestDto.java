package com.daou.sabangnetserver.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenRequestDto {

    private String apiKey;
    private String sltnCd;

}
