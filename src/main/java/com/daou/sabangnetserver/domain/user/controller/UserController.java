package com.daou.sabangnetserver.domain.user.controller;

import com.daou.sabangnetserver.domain.auth.dto.LoginRequestDto;
import com.daou.sabangnetserver.domain.user.dto.UserDeleteRequestDto;
import com.daou.sabangnetserver.domain.user.dto.UserDto;
import com.daou.sabangnetserver.domain.user.dto.UserRegisterRequestDto;
import com.daou.sabangnetserver.domain.user.dto.UserSearchRequestDto;
import com.daou.sabangnetserver.domain.user.dto.UserUpdateMeRequestDto;
import com.daou.sabangnetserver.domain.user.dto.UserUpdateOthersRequestDto;
import com.daou.sabangnetserver.domain.user.dto.UserUpdatePasswordDto;
import com.daou.sabangnetserver.domain.user.service.UserService;
import com.daou.sabangnetserver.global.common.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<SuccessResponse> searchUsers(@Valid @ModelAttribute UserSearchRequestDto requestDto) {
        return ResponseEntity.ok(SuccessResponse.builder()
                .code(HttpStatus.OK.value())
                .message("유저를 성공적으로 조회했습니다.")
                .data(userService.searchUsers(requestDto))
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse> registerUser(@Valid @RequestBody UserRegisterRequestDto requestDto) {
        userService.registerUser(requestDto);
        return ResponseEntity.ok(SuccessResponse.builder()
                .code(HttpStatus.OK.value())
                .message("관리자가 정상적으로 등록되었습니다.")
                .build());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<SuccessResponse> deleteUsers(@RequestBody UserDeleteRequestDto requestDto){
        userService.deleteUser(requestDto);
        return ResponseEntity.ok(SuccessResponse.builder()
                .code(HttpStatus.OK.value())
                .message("관리자가 정상적으로 삭제되었습니다.")
                .build());
    }

    @PatchMapping("/update/others")
    public ResponseEntity<SuccessResponse> updateOtherUser(@Valid @RequestBody UserUpdateOthersRequestDto requestDto){
        userService.updateOtherUser(requestDto);
        return ResponseEntity.ok(SuccessResponse.builder()
                .code(HttpStatus.OK.value())
                .message("관리자가 정상적으로 수정되었습니다.")
                .build());

    }

    @PatchMapping("/update/password")
    public ResponseEntity<SuccessResponse> updatePassword(HttpServletRequest httpServletRequest, @Valid @RequestBody UserUpdatePasswordDto requestDto){
        userService.updatePassword(requestDto, httpServletRequest.getHeader("Authorization").replace("Bearer ", ""));
        return ResponseEntity.ok(SuccessResponse.builder()
                .code(HttpStatus.OK.value())
                .message("비밀번호가 정상적으로 수정되었습니다.")
                .build());
    }

    @PatchMapping("/update/me")
    public ResponseEntity<SuccessResponse> updateMe(HttpServletRequest httpServletRequest, @Valid @RequestBody UserUpdateMeRequestDto requestDto){
        userService.updateMe(requestDto, httpServletRequest.getHeader("Authorization").replace("Bearer ", ""));
        return ResponseEntity.ok(SuccessResponse.builder()
                .code(HttpStatus.OK.value())
                .message("정상적으로 수정되었습니다.")
                .build());
    }

    @GetMapping("/mypage/{id}")
    public ResponseEntity<SuccessResponse> getUserInfo(@PathVariable String id) {
        UserDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(SuccessResponse.builder()
                .code(HttpStatus.OK.value())
                .message("유저 정보를 성공적으로 조회했습니다.")
                .data(userDto)
                .build());
    }

    @PostMapping("/checkpassword")
    public ResponseEntity<SuccessResponse> checkPassword(HttpServletRequest request, @RequestBody LoginRequestDto loginRequestDto) {
        userService.checkPassword(request,loginRequestDto);
        return ResponseEntity.ok(SuccessResponse.builder()
                .code(HttpStatus.OK.value())
                .message("비밀번호가 일치합니다.")
                .build());
    }
}
