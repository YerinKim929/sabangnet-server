package com.daou.sabangnetserver.domain.user.service;

import com.daou.sabangnetserver.domain.auth.dto.ApproveRequestDto;
import com.daou.sabangnetserver.domain.user.dto.UserDto;
import com.daou.sabangnetserver.domain.user.dto.UserRegisterRequestDto;
import com.daou.sabangnetserver.domain.user.dto.UserSearchRequestDto;
import com.daou.sabangnetserver.domain.user.dto.UserSearchResponseDto;
import com.daou.sabangnetserver.domain.user.dto.*;
import com.daou.sabangnetserver.domain.user.entity.Authority;
import com.daou.sabangnetserver.domain.user.entity.User;
import com.daou.sabangnetserver.domain.user.repository.UserRepository;
import com.daou.sabangnetserver.domain.user.util.SecurityUtil;
import com.daou.sabangnetserver.global.error.DuplicationException;
import com.daou.sabangnetserver.global.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

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

        String authority = Objects.requireNonNull(user.getAuthorities().stream()
                .findFirst()
                .map(Authority::getAuthorityName)
                .orElse(null)).substring(5);

        return UserDto.builder()
                .userId(user.getUserId())
                .authority(authority)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .memo(user.getMemo())
                .department(user.getDepartment())
                .registrationDate(user.getRegistrationDate())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .isUsed(user.getIsUsed())
                .build();
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

        List<UserDto> userDtos = userPage.getContent().stream().map(this::convertToDto).toList();

        return UserSearchResponseDto.of(userPage.getNumber(), (int) userPage.getTotalElements(), userPage.getTotalPages(), userDtos);
    }

    @Transactional
    public void registerUser(UserRegisterRequestDto requestDto){

        if (userRepository.existsByIdAndIsDeleteFalse(requestDto.getId())) {
            throw new DuplicationException(HttpStatus.BAD_REQUEST.value(), "이미 존재하는 아이디입니다.");
        }

        if (userRepository.existsByEmailAndIsDeleteFalse(requestDto.getEmail())) {
            throw new DuplicationException(HttpStatus.BAD_REQUEST.value(), "이미 존재하는 이메일입니다.");
        }


        LocalDateTime registrationDate = LocalDateTime.now().withNano(0);

        //권한 정보 생성
        Authority authority = authorityRepository.findByAuthorityName("MASTER".equals(requestDto.getAuthority()) ? "ROLE_MASTER" : "ROLE_ADMIN").orElseThrow(()-> new AuthorityNotFoundException(HttpStatus.FORBIDDEN.value(), "등록할 권한이 존재하지 않습니다."));;


        User user = User.builder()
                .id(requestDto.getId()) // 아이디
                .pw(passwordEncoder.encode(requestDto.getPassword())) // 비밀번호 (암호화 해서 가져옴)
                .name(requestDto.getName()) // 이름
                .email(requestDto.getEmail())
                .department(requestDto.getDepartment())
                .memo(requestDto.getMemo())
                .registrationDate(registrationDate)
                .isUsed(true)  // master 승인 로직 api 완료 시 false로 변경
                .isDelete(false)
                .authorities(Collections.singleton(authority))
                .build();

        userRepository.save(user);
    }

    @Transactional
    public void updateIsUsed(ApproveRequestDto requestDto) {
        List<String> ids = requestDto.getIds();
        ids.stream()
                .map(userRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(user -> !user.getIsUsed())
                .forEach(user -> {
                    user.updateIsUsed();
                    userRepository.save(user);
                });
    }

    @Transactional
    public void deleteUser(UserDeleteRequestDto requestDto){

        for(String id : requestDto.getIds()){
            User user = userRepository.findById(id).orElseThrow( ()-> new RuntimeException("삭제할 아이디가 존재하지 않습니다."));
            user.deleteUser();
        }
    }

    @Transactional
    public void updateOtherUser(UserUpdateOthersRequestDto requestDto){

        User user = userRepository.findById(requestDto.getId()).orElseThrow(()-> new RuntimeException("수정할 아이디가 존재하지 않습니다."));

        if (!user.getEmail().equals(requestDto.getEmail()) && userRepository.existsByEmailAndIsDeleteFalse(requestDto.getEmail()))
            throw new RuntimeException("이미 존재하는 이메일입니다.");

        user.updateUserInfo(requestDto);
    }


    @Transactional
    public void updatePassword(UserUpdatePasswordDto requestDto, String jwt){
        if(requestDto.getCurrentPassword().equals(requestDto.getNewPassword()))
            throw new RuntimeException("변경할 비밀번호가 동일합니다.");

        String id = tokenProvider.getIdFromToken(jwt);
        User user = userRepository.findById(id).orElseThrow(()-> new RuntimeException("아이디가 존재하지 않습니다."));

        user.updatePassword(bCryptPasswordEncoder.encode(requestDto.getNewPassword()));
    }

    @Transactional
    public void updateMe(UserUpdateMeRequestDto requestDto, String jwt){
        String id = tokenProvider.getIdFromToken(jwt);
        User user = userRepository.findById(id).orElseThrow(()-> new RuntimeException("아이디가 존재하지 않습니다."));

        if (!user.getEmail().equals(requestDto.getEmail()) && userRepository.existsByEmailAndIsDeleteFalse(requestDto.getEmail()))
            throw new RuntimeException("이미 존재하는 이메일입니다.");

        user.updateUserInfo(requestDto);
    }
}
