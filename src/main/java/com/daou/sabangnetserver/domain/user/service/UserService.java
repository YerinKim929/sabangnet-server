/*UserController에 대한 서비스*/
package com.daou.sabangnetserver.domain.user.service;

import com.daou.sabangnetserver.domain.user.dto.UserDto;
import com.daou.sabangnetserver.domain.user.dto.UserSearchRequestDto;
import com.daou.sabangnetserver.domain.user.dto.UserSearchResponseDto;
import com.daou.sabangnetserver.domain.user.entity.Authority;
import com.daou.sabangnetserver.domain.user.entity.User;
import com.daou.sabangnetserver.domain.user.repository.UserRepository;
import com.daou.sabangnetserver.domain.user.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;



    //user 등록하는 메소드
    @Transactional
    public User signup(UserDto userDto) {
        if(userRepository.findOneWithAuthoritiesById(userDto.getId()).orElse(null) != null) {
            throw new RuntimeException("already exist");
        }

        //사용자 정보가 존재하지 않는 경우
        //권한 정보 생성
        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();

        //유저 정보 생성해 저장 (요소 변경 필요)
        User user = User.builder()
                .id(userDto.getId()) //아이디
                .pw(passwordEncoder.encode(userDto.getPassword())) //비밀번호 (암호화 해서 가져옴)
                .name(userDto. getName()) //이름
                .authorities(Collections.singleton(authority)) //권한
                .activated(true)
                .build();

        return userRepository.save(user);

    }

    //유저 및 권한 정보를 가져오는 메소드
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities(String username) {
        return userRepository.findOneWithAuthoritiesById(username);
    }

    //현재 securityContext에 저장된 유저 정보만 가져옴
    @Transactional(readOnly = true)
    public Optional<User> getMyUserWithAuthorities () {
        return SecurityUtil.getCurrentUsername()
                .flatMap(userRepository::findOneWithAuthoritiesById);
    }


    private UserDto convertToDto(User user){
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setPermission(user.getPermission());
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setMemo(user.getMemo());
        userDto.setDepartment(user.getDepartment());
        userDto.setRegistrationDate(user.getRegistrationDate());
        userDto.setLastLoginTime(user.getLastLoginTime());
        userDto.setLastLoginIp(user.getLastLoginIp());
        userDto.setIsUsed(user.getIsUsed());

        return userDto;
    }


    public UserSearchResponseDto searchUsers(UserSearchRequestDto requestDto){

        Pageable pageable = PageRequest.of(requestDto.getPage() - 1, requestDto.getShowList());

        Page<User> userPage = userRepository.searchUsers(
                requestDto.getId(),
                requestDto.getName(),
                requestDto.getEmail(),
                requestDto.getIsUsed(),
                pageable
        );


        List<UserDto> userDtos = userPage.getContent().stream().map(this::convertToDto).collect(Collectors.toList());

        UserSearchResponseDto responseDto = UserSearchResponseDto.of(userPage.getNumber(), (int) userPage.getTotalElements(), userDtos);

        return responseDto;
    }



}
