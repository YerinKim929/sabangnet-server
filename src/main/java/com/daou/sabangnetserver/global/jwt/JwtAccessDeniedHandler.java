/*필요한 권한 없이 접근하는 경우 403에러 리턴*/
package com.daou.sabangnetserver.global.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse,
                       AccessDeniedException accessDeniedException) throws IOException{
        httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
