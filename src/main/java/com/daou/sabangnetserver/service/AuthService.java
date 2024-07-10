package com.daou.sabangnetserver.service;

import com.daou.sabangnetserver.dto.TokenRequestDto;
import com.daou.sabangnetserver.dto.TokenResponseDto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class AuthService {
    @Value("${external.api.base-url}")
    private String baseUrl;

    @Value("${seller.no}")
    private String sellerNo;

    private final RestTemplate restTemplate = new RestTemplate();

    public TokenResponseDto createTokens(TokenRequestDto request) {
        String url = String.format("%s/%s/account/tokens/create", baseUrl, sellerNo);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TokenRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<TokenResponseDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, TokenResponseDto.class);
        return responseEntity.getBody();
    }
}

