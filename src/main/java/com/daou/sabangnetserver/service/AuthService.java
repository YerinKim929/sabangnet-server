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

    // 토큰 데이터 저장을 위한 tokensRepository DI
    @Autowired
    private TokensRepository tokensRepository;

    // 환경 변수 값 불러오기
    @Value("${external.api.base-url}")
    private String baseUrl; // 기본 url

    // RestTemplate 인스턴스 생성
    private final RestTemplate restTemplate = new RestTemplate();

    // 토큰 검증 함수
    // 토큰 유효기간 판단 후 작업 수행 (토큰 갱신/발급)
    public TokenResponseDto validateAndRefreshTokens(TokenRequestDto request, int sellerNo) {

        Optional<Tokens> existingTokensOpt = tokensRepository.findById(sellerNo);

        // sellerNo 기준 토큰 데이터가 테이블 내에 존재할 시,
        if (existingTokensOpt.isPresent()) {
            Tokens existingTokens = existingTokensOpt.get(); // 토큰 객체 받기
            LocalDateTime now = LocalDateTime.now(); // 현재시간 받기

            // 현재 시각 기준 accessToken이 유효하면, 테이블 내 토큰 반환
            if (isAccessTokenValid(existingTokens, now)) {
                return returnExistingTokens(existingTokens);
            }

            // 현재 시각 기준 accessToken이 만료되고 RefreshToken이 유효하면, accessToken 새로 발급
            if (isRefreshTokenValid(existingTokens, now)) {
                return refreshAccessToken(existingTokens, sellerNo);
            }
        }
        // 현재 시각 기준 accessToken 및 RefreshToken이 만료되면 토큰 새로 발급
        // 또는, 테이블 내 사용자 토큰 정보 없을 시 토큰 새로 발급
        return createAndSaveNewTokens(request, sellerNo);
    }


    // Access Token이 유효한지 검사
    private boolean isAccessTokenValid(Tokens tokens, LocalDateTime now) {
        return now.isBefore(tokens.getExpiresAt());
    }

    // Refresh Token이 유효한지 검사
    private boolean isRefreshTokenValid(Tokens tokens, LocalDateTime now) {
        return now.isBefore(tokens.getRefreshExpiresAt());
    }

    //테이블 내 토큰 반환
    private TokenResponseDto returnExistingTokens(Tokens tokens) {
        log.info("토큰이 유효합니다. Access Token: {}", tokens.getAccessToken());
        return new TokenResponseDto(tokens.getAccessToken(), tokens.getRefreshToken());
    }

    // Access Token 갱신
    private TokenResponseDto refreshAccessToken(Tokens tokens, int sellerNo) {
        log.info("엑세스 토큰이 만료되었습니다. 새로운 엑세스 토큰을 요청합니다.");

        TokenResponseDto newAccessTokenResponse = requestNewAccessToken(sellerNo, tokens.getRefreshToken());
        updateToken(tokens, newAccessTokenResponse);

        log.info("새로운 엑세스 토큰이 발급되었습니다. Access Token: {}", newAccessTokenResponse.getAccessToken());
        return newAccessTokenResponse;
    }


    // 새로운 토큰 발급 및 저장
    private TokenResponseDto createAndSaveNewTokens(TokenRequestDto request, Integer sellerNumber) {
        log.info("토큰 정보가 없거나 리프레시 토큰이 만료되었습니다. 새로운 토큰을 요청합니다.");

        TokenResponseDto newTokensResponse = requestNewTokens(request, sellerNumber);
        saveOrUpdateTokens(newTokensResponse, sellerNumber);

        return newTokensResponse;
    }


    // 토큰이 테이블에 없거나 모든 토큰이 유효기간이 지났을 때, 새로 발급된 토큰을 DB에 저장하는 메소드
    private void saveOrUpdateTokens(TokenResponseDto newTokensResponse, Integer sellerNumber) {

        Optional<Tokens> existingTokensOpt = tokensRepository.findById(sellerNumber);

        if (existingTokensOpt.isPresent()) {
            Tokens existingTokens = existingTokensOpt.get();
            updateToken(existingTokens, newTokensResponse);
        } else {
            saveNewToken(newTokensResponse, sellerNumber);
        }
    }

    // 새로운 토큰 저장
    private void saveNewToken(TokenResponseDto newTokensResponse, Integer sellerNumber) {
        Tokens newTokens = Tokens.builder()
                .sellerNo(sellerNumber)
                .accessToken(newTokensResponse.getAccessToken())
                .refreshToken(newTokensResponse.getRefreshToken())
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(3))
                .refreshExpiresAt(LocalDateTime.now().plusDays(1))
                .build();
        tokensRepository.save(newTokens);
    }

    // 새로운 토큰 데이터로 테이블 최신화
    private void updateToken(Tokens tokens, TokenResponseDto tokenResponse) {
        tokens.setAccessToken(tokenResponse.getAccessToken());
        tokens.setRefreshToken(tokenResponse.getRefreshToken());
        tokens.setIssuedAt(LocalDateTime.now());
        tokens.setExpiresAt(LocalDateTime.now().plusHours(3));
        tokens.setRefreshExpiresAt(LocalDateTime.now().plusDays(1));
        tokensRepository.save(tokens);
    }


    // AccessToken을 발급받는 메소드 (Refresh Token 사용)
    private TokenResponseDto requestNewAccessToken(int sellerNo, String refreshToken) {
        String url = String.format("%s/openapi/v2/seller/%s/account/tokens/refresh", baseUrl, sellerNo);

        // 요청 Http 객체 선언
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", refreshToken); // 헤더에 리프레시 토큰 추가

        HttpEntity<String> entity = new HttpEntity<>("", headers); // 빈 바디 명시

        //응답 Http 객체 선언
        ResponseEntity<TokenResponseDto> responseEntity;
        // request 요청 보내고 응답 받기
        responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, TokenResponseDto.class);

        return responseEntity.getBody();
    }

    // 토큰 자체를 새로 발급 받는 메소드
    private TokenResponseDto requestNewTokens(TokenRequestDto request, int sellerNo) {
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
