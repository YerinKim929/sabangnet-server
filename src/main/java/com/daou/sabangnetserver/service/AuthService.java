package com.daou.sabangnetserver.service;

import com.daou.sabangnetserver.dto.TokenRequestDto;
import com.daou.sabangnetserver.dto.TokenResponseDto;
import com.daou.sabangnetserver.model.Tokens;
import com.daou.sabangnetserver.repository.TokensRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private TokensRepository tokensRepository;

    @Value("${external.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // 토큰 검증 함수
    // 토큰 유효기간 판단 후 작업 수행 1) 토큰 발급 , 2) 토큰 갱신
    public TokenResponseDto validateAndRefreshTokens(TokenRequestDto request, String sellerNo) {
        Integer sellerNumber = Integer.parseInt(sellerNo);
        Optional<Tokens> existingTokensOpt = tokensRepository.findById(sellerNumber);

        // 토큰 값이 테이블 내에 존재할 시,
        if (existingTokensOpt.isPresent()) {
            Tokens existingTokens = existingTokensOpt.get(); // 토큰 객체 받기
            LocalDateTime now = LocalDateTime.now(); // 현재시간 받기

            // 현재 시각이 access token의 유효기간 이전 일 때, 테이블 내 토큰 반환
            if (now.isBefore(existingTokens.getExpiresAt())) {
                log.info("토큰이 유효합니다. Access Token: {}", existingTokens.getAccessToken());
                return new TokenResponseDto(existingTokens.getAccessToken(), existingTokens.getRefreshToken());
            }

            // 현재 시각이 Refresh token의 유효기간 이전 일 때, access token 새로 발급
            else if (now.isBefore(existingTokens.getRefreshExpiresAt())) {
                log.info("엑세스 토큰이 만료되었습니다. 새로운 엑세스 토큰을 요청합니다.");
                // requestNewAccessToken 메소드 실행하여 새로운 토큰 값 받기
                // 토큰 값을 현재 테이블 속 토큰으로 대체 및 DB 저장
                TokenResponseDto newAccessTokenResponse = requestNewAccessToken(sellerNo, existingTokens.getRefreshToken());
                updateToken(existingTokens, newAccessTokenResponse);
                tokensRepository.save(existingTokens);

                log.info("새로운 엑세스 토큰이 발급되었습니다. Access Token: {}", newAccessTokenResponse.getAccessToken());
                return newAccessTokenResponse;
            }
        }
        // 토큰이 테이블에 없거나 모든 토큰이 유효기간이 지났을 때, 새로운 토큰 발급
        // createAndSaveOrUpdateTokens 메소드 실행하여 새로운 토큰 값 받기
        log.info("토큰 정보가 없거나 리프레시 토큰이 만료되었습니다. 새로운 토큰을 요청합니다.");
        return createAndSaveOrUpdateTokens(request, sellerNumber);
    }

    // 토큰이 테이블에 없거나 모든 토큰이 유효기간이 지났을 때, 새로 발급된 토큰을 DB에 저장하는 메소드
    // requestNewTokens 메소드를 통해 새로운 토큰을 발급한다.
    private TokenResponseDto createAndSaveOrUpdateTokens(TokenRequestDto request, Integer sellerNumber) {
        TokenResponseDto newTokensResponse = requestNewTokens(request, sellerNumber.toString());
        Optional<Tokens> existingTokensOpt = tokensRepository.findById(sellerNumber);
        Tokens tokens;

        // 토큰에 값이 존재하면, updateToken 메소드를 통해 토큰 업데이트
        if (existingTokensOpt.isPresent()) {
            tokens = existingTokensOpt.get();
            updateToken(tokens, newTokensResponse);
        } else { // 토큰이 테이블에 없다면 builder를 통해 초기 값 세팅
            tokens = Tokens.builder()
                    .sellerNo(sellerNumber)
                    .accessToken(newTokensResponse.getAccessToken())
                    .refreshToken(newTokensResponse.getRefreshToken())
                    .issuedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(3))
                    .refreshExpiresAt(LocalDateTime.now().plusDays(1))
                    .build();
        }

        // 토큰 테이블 삽입
        tokensRepository.save(tokens);

        log.info("새로운 토큰이 발급되었습니다. Access Token: {}, Refresh Token: {}", newTokensResponse.getAccessToken(), newTokensResponse.getRefreshToken());
        return newTokensResponse;
    }

    // 토큰 관련 값 테이블 최신화
    private void updateToken(Tokens tokens, TokenResponseDto tokenResponse) {
        tokens.setAccessToken(tokenResponse.getAccessToken());
        tokens.setRefreshToken(tokenResponse.getRefreshToken());
        tokens.setIssuedAt(LocalDateTime.now());
        tokens.setExpiresAt(LocalDateTime.now().plusHours(3));
        tokens.setRefreshExpiresAt(LocalDateTime.now().plusDays(1));
    }

    // AccessToken을 발급받는 메소드 (Refresh Token 사용)
    private TokenResponseDto requestNewAccessToken(String sellerNo, String refreshToken) {
        String url = String.format("%s/openapi/v2/seller/%s/account/tokens/refresh", baseUrl, sellerNo);

        // 요청 Http 객체 선언
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", refreshToken); // 헤더에 리프레시 토큰 추가

        HttpEntity<String> entity = new HttpEntity<>("", headers); // 빈 바디 명시

        //응답 Http 객체 선언
        ResponseEntity<TokenResponseDto> responseEntity;
        // request 요청 보내고 응답 받기
        responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, TokenResponseDto.class);

        return responseEntity.getBody();
    }

    // 토큰 자체를 새로 발급 받는 메소드
    private TokenResponseDto requestNewTokens(TokenRequestDto request, String sellerNo) {
        String url = String.format("%s/openapi/v2/seller/%s/account/tokens/create", baseUrl, sellerNo);

        // 요청 Http 객체 선언
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TokenRequestDto> entity = new HttpEntity<>(request, headers); // request => apikey,sltnCd

        //응답 Http 객체 선언
        ResponseEntity<TokenResponseDto> responseEntity;
        // request 요청 보내고 응답 받기
        responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, TokenResponseDto.class);

        return responseEntity.getBody();
    }
}
