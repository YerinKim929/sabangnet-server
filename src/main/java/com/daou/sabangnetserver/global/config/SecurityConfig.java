package com.daou.sabangnetserver.global.config;

import com.daou.sabangnetserver.global.jwt.JwtAccessDeniedHandler;
import com.daou.sabangnetserver.global.jwt.JwtAuthenticationEntryPoint;
import com.daou.sabangnetserver.global.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



/**
 *api 주소 권한 설정 config
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler; //권한이 존재하지 않는 경우 403 Forbidden 에러 리턴;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; //자격 증명 없이 접근 시 401 Unauthorized 에러 리턴;


    //PasswordEncoder로 BCryptEncoder 사용
    //Password 평문으로 저장 X, 비밀번호에 salt값을 추가해 해시를 생성함; 같은 비밀번호를 여러 계정에 사용해도 해시 값이 동일하지 X
    //JWT 생성 및 검증 단계에서 사용 X, 로그인 단계에 필요
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(AbstractHttpConfigurer::disable) //cors 사용 X
                .csrf(AbstractHttpConfigurer::disable) //csrf 사용 X

                //예외 처리
                .exceptionHandling(exception -> exception.
                        authenticationEntryPoint(jwtAuthenticationEntryPoint).
                        accessDeniedHandler(jwtAccessDeniedHandler))


                //h2-database 사용 가능 처리
                .headers((headerConfig)->
                        headerConfig.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))


                .authorizeHttpRequests((authorize) -> authorize //HttpServletRequest를 사용하는 요청들에 대한 접근 제한
                    .requestMatchers("/login").permitAll() //로그인 api
                    .requestMatchers("/search").permitAll() //관리자 조회 api
                    .requestMatchers("/register").permitAll() //관리자 등록 api
                    .requestMatchers("/history").permitAll() // 로그 조회 api
                    .requestMatchers("/logout").permitAll() //로그아웃 api
                    .requestMatchers("/authenticate").permitAll()
                    .anyRequest()
                    .authenticated())//위의 api 제외 토큰 인증없이 접근 X - api 목록 권한 수정 필요

                .sessionManagement (session -> session.
                        sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 X

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);



                return httpSecurity.build();
    }
}
