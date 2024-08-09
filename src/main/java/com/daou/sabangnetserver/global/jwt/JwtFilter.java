/*검증이 완료된 JWT로부터 유저정보 받아서 UsernamePasswordAuthenticationFilter로 전달
* 인증 작업을 진행하는 Filter에서 해당 컴포넌트 사용*/
/*oncePerRequestFilter 상속받음*/


package com.daou.sabangnetserver.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private final TokenProvider tokenProvider;

    //토큰 인증정보를 SecurityContext에 저장
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = resolveToken(request);
        String requestURI = request.getRequestURI();

        //토큰 정보가 null이 아니고 유효한 토큰인 것이 검증 되는 경우
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            //인증 정보 가져와서 Security Context Holder에 저장
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Security Context에 '{}' 인증정보를 저장했습니다, uri : {}", authentication.getName(), requestURI);
        } else {
            logger.debug("유효한 토큰이 존재하지 않습니다, uri : '{}'", requestURI);
        }
        filterChain.doFilter(request, response);
    }

    //Request Header에서 토큰 정보 꺼내옴
    private String resolveToken(HttpServletRequest httpServletRequest) {
        String bearerToken = httpServletRequest.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

//    @Override
//    protected boolean shouldNotFilter

}
