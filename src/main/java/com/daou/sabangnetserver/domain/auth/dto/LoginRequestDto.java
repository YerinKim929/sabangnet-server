package com.daou.sabangnetserver.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginRequestDto {
    @NotBlank(message = "아이디는 필수적으로 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9]{3,60}$", message = "아이디는 최소 3자 이상, 60자 이하의 영문 혹은 영문과 숫자를 조합해주세요.")
    private String id; //아이디


    @NotBlank(message = "비밀번호는 필수적으로 입력해주세요.")
    private String password;

}
