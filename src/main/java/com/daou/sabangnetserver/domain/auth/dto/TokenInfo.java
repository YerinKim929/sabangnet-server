/*클라이언트에 토큰 전송을 위한 DTO*/
/*spring security 사용 시에는 따로 사용하지 않는 듯; 삭제 여부 결정하기*/
package com.daou.sabangnetserver.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenInfo {
    private String accessToken;
    private String refreshToken;
}
