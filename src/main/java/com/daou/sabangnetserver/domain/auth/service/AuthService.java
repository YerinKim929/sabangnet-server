package com.daou.sabangnetserver.domain.auth.service;

import com.daou.sabangnetserver.domain.auth.dto.AuthResponseDto;
import com.daou.sabangnetserver.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    public AuthResponseDto extractIdAndAuthority () {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authentication.getPrincipal();
        String id = user.getId();

        GrantedAuthority grantedAuthority = authentication.getAuthorities().iterator().next();
        String authorityName = grantedAuthority.getAuthority();


        return AuthResponseDto.builder()
                .id(id)
                .authority(authorityName.substring(5))
                .build();
    }

}
