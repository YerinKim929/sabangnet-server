package com.daou.sabangnetserver.domain.user.service;

import com.daou.sabangnetserver.domain.user.entity.User;
import com.daou.sabangnetserver.domain.user.repository.UserRepository;
import com.daou.sabangnetserver.global.error.UserNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component("userDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
//    loadUserByUsername에서의 username = 로그인 시 사용하는 id
//    로그인 시 DB에서 유저 정보 및 권한을 가져와서 userdetails.User 객체 생성, 반환
    public UserDetails loadUserByUsername(final String username) throws UserNotFoundException {
        return userRepository.findOneWithAuthoritiesByIdAndIsDeleteFalse(username)
                .map(this::createUser) //해당 user 제외 전부 user entity사용
                .orElseThrow(() -> new UserNotFoundException(HttpStatus.NOT_FOUND.value(), username + " 를 데이터 베이스에서 찾을 수 없습니다."));
    }

    private org.springframework.security.core.userdetails.User createUser(User user) {
        List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(user.getId(), user.getPw(),grantedAuthorities);
    }
}
