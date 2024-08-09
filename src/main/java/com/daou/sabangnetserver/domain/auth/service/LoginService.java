package com.daou.sabangnetserver.domain.auth.service;

import com.daou.sabangnetserver.domain.auth.dto.LoginRequestDto;
import com.daou.sabangnetserver.domain.auth.dto.LoginResponseDto;
import com.daou.sabangnetserver.domain.auth.dto.LoginServiceDto;
import com.daou.sabangnetserver.domain.auth.utils.LookUpHttpHeader;
import com.daou.sabangnetserver.domain.user.entity.History;
import com.daou.sabangnetserver.domain.user.entity.User;
import com.daou.sabangnetserver.domain.user.repository.HistoryRepository;
import com.daou.sabangnetserver.domain.user.repository.UserRepository;
import com.daou.sabangnetserver.global.error.AuthorityNotFoundException;
import com.daou.sabangnetserver.global.error.UserNotFoundException;
import com.daou.sabangnetserver.global.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class LoginService {

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepo;
    private final HistoryRepository historyRepo;

    public LoginResponseDto validateLogin(HttpServletRequest request, LoginRequestDto loginRequestDto) {

        LookUpHttpHeader lookUpHttpHeader = new LookUpHttpHeader();

        LoginServiceDto loginServiceDto = LoginServiceDto.builder()
                .id(loginRequestDto.getId())
                .password(loginRequestDto.getPassword())
                .loginIp(lookUpHttpHeader.getIpAddress(request))
                .loginDevice(lookUpHttpHeader.getLoginDeviceInfo(request))
                .loginTime(LocalDateTime.now().withNano(0))
                .build();

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(loginServiceDto.getId(),
                        loginServiceDto.getPassword());

        Authentication authentication;
        try {
            // authenticate 메소드 실행시 CustomDetailsService 클래스의 loadUserByUsername 메소드 실행
            authentication = authenticationManagerBuilder.getObject().authenticate(usernamePasswordAuthenticationToken);
            // 해당 객체를 SecurityContextHolder에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException e) {
            throw new RuntimeException("아이디 혹은 비밀번호가 일치하지 않습니다.");
        }

        String jwt = "Bearer " + tokenProvider.generateToken(authentication);

        User user = updateUserInfoAndReturnUser(loginServiceDto);
        insertHistory(loginServiceDto, user);

        return new LoginResponseDto(jwt);
    }

    @Transactional
    private User updateUserInfoAndReturnUser(LoginServiceDto loginServiceDto){
        User user = userRepo.findByIdAndIsDeleteFalse(loginServiceDto.getId()).orElseThrow(
                ()->new UserNotFoundException(HttpStatus.NOT_FOUND.value(), "해당 사용자가 없습니다.")
        );

        if (!user.getIsUsed()) {
            throw new AuthorityNotFoundException(HttpStatus.FORBIDDEN.value(), "사용자가 활성화되지 않았습니다.");
        }

        user.updateLastLoginInfo(loginServiceDto.getLoginIp(), loginServiceDto.getLoginTime());

        return user;
    }

    @Transactional
    private void insertHistory(LoginServiceDto loginServiceDto, User user){
        historyRepo.save(History.builder()
                .loginIp(loginServiceDto.getLoginIp())
                .loginDevice(loginServiceDto.getLoginDevice())
                .user(user)
                .loginTime(loginServiceDto.getLoginTime())
                .build()
        );
    }
}
