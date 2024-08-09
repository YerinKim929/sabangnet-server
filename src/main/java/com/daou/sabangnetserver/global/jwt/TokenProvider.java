/*토큰 발급 및 검증 수행 */
package com.daou.sabangnetserver.global.jwt;

import com.daou.sabangnetserver.domain.user.entity.Authority;
import com.daou.sabangnetserver.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
/*위의 패키지 내용 확인하기*/

@Slf4j
@Component
public class TokenProvider implements InitializingBean {

    private Key key;

    private static final String AUTHORITIE_KEY = "auth";
    private final String secretKey;
    private final long tokenExpirationInMilliSeconds;

    //yml의 key값과 만료시간 가져오기; 시간 밀리초로 변환
    public TokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.token-expiration-in-seconds}") long tokenExpirationInSeconds){
                this.secretKey = secretKey;
                this.tokenExpirationInMilliSeconds = tokenExpirationInSeconds * 3000;
    }


    //bean이 생성되고 주입 받은 후, secretKey값을 Base64로 디코딩
    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /*유저 인증 정보를 가지고 와 AccessToken을 생성하는 메소드*/

    public String generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        //인증 객체에서 사용자의 권한 정보를 가져옴

        long now = (new Date()).getTime();
        Date accessTokenSetIn = new Date (now);
        Date accessTokenExpiresIn = new Date (now + this.tokenExpirationInMilliSeconds);
        //token의 만료 시간 설정

        return Jwts.builder()
                .subject(authentication.getName())
                .claim(AUTHORITIE_KEY, authorities)
                .issuedAt(accessTokenSetIn)//token 발행 시간 정보
                .expiration(accessTokenExpiresIn)//token 만료 시간 정보; 해당 옵션 삭제 시 만료 X
                .signWith(this.key)
                .compact();
    }

    //JWT 토큰으로 claim 생성 및 유저 객체 생성해 최종적으로 authentication 객체 리턴
    public Authentication getAuthentication(String accessToken) {
        Claims claims = Jwts
                .parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();


        //claim의 권한 정보 가져오기
        Collection<?extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIE_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Set<Authority> authoritiesSet = authorities.stream()
                .map(authority -> new Authority(authority.getAuthority())) // Authority 생성자가 필요함
                .collect(Collectors.toSet());

        //User로 Authentication 리턴
        User principal = User.builder()
                .id(claims.getSubject())
                .pw("")
                .name("")
                .email("")
                .department(null)
                .memo(null)
                .isUsed(null)
                .lastLoginIp("")
                .lastLoginTime(LocalDateTime.now().withNano(0))
                .registrationDate(LocalDateTime.now().withNano(0))
                .authorities(authoritiesSet)
                .isDelete(false)
                .build();

        return new UsernamePasswordAuthenticationToken(principal, accessToken, authorities);

    }

    //토큰 정보(유효성) 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 토큰입니다", e);
        } catch (ExpiredJwtException e) {
            log.info("만료된 토큰입니다", e);
        } catch (UnsupportedJwtException e) {
            log.info("지원하지 않는 토큰입니다", e);
        } catch (IllegalArgumentException e) {
            log.info("빈 토큰입니다", e);
        }
        return false;
    }

    // 토큰에서 Authority와 userId 추출
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String authorities = claims.get("auth", String.class);

        return authorities == null ? List.of() :
                Arrays.stream(authorities.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
    }

}

