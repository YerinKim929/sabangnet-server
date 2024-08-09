package com.daou.sabangnetserver.global.log;

import com.daou.sabangnetserver.domain.auth.utils.LookUpHttpHeader;
import com.daou.sabangnetserver.domain.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final LookUpHttpHeader lookUpHttpHeader;

    @Pointcut("within(com.daou.sabangnetserver.domain.auth.controller..*)")
    public void authController() {
    }

    @Pointcut("within(com.daou.sabangnetserver.domain.user.controller..*)")
    public void userController() {
    }

    @Pointcut("authController() || userController()")
    public void applicationController() {
    }

    @Around("applicationController()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        LocalDateTime loginTime = LocalDateTime.now().withNano(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = loginTime.format(formatter);

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String loginIp = lookUpHttpHeader.getIpAddress(request);
        String loginDevice = lookUpHttpHeader.getLoginDeviceInfo(request);

        String loginId = "N/A";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof User user) {
            loginId = user.getId();
        }

        // Before
        log.info("=== Start Request {} ===", joinPoint.getSignature().toShortString());
        log.info("Time: {} / Login ID: {}", formattedDateTime, loginId);
        log.info("IP Address: {} / Device: {}", loginIp, loginDevice);

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            // AfterThrowing
            log.error("=== Error In Request {} ===", joinPoint.getSignature().toShortString());
            log.info("Time: {} / Login ID: {}", formattedDateTime, loginId);
            log.info("IP Address: {} / Device: {}", loginIp, loginDevice);
            log.error("ERROR: {}", throwable.getMessage());
            throw throwable;
        }

        // AfterReturning
        log.info("=== End Request {} ===", joinPoint.getSignature().toShortString());
        log.info("Time: {} / Login ID: {}", formattedDateTime, loginId);
        log.info("IP Address: {} / Device: {}", loginIp, loginDevice);
        if (result != null) {
            log.info("RESULT: {}", result);
        }
        return result;
    }

}
