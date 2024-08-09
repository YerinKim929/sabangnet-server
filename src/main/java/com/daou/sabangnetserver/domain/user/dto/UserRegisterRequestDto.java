package com.daou.sabangnetserver.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UserRegisterRequestDto {
    @NotBlank(message = "아이디는 필수로 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9]{3,60}$", message = "아이디는 최소 3자 이상, 60자 이하의 영문 혹은 영문과 숫자를 조합해주세요.")
    private String id;

    @NotBlank(message = "비밀번호는 필수로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*]).{10,100}$", message = "비밀번호는 10자 이상, 100자 이하의 영소문자/숫자/특수문자를 조합해주세요.")
    private String password;

    @NotBlank(message = "관리자 권한은 필수로 입력해주세요.")
    @Pattern(regexp = "^(ADMIN|MASTER)$", message = "권한은 ADMIN 또는 MASTER만 허용됩니다.")
    private String authority;

    @NotBlank(message = "이름은 필수로 입력해주세요.")
    private String name;

    @NotBlank(message = "이메일은 필수로 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "이메일 형식에 맞게 작성해주십시오.")
    private String email;

    private String department;
    private String memo;
}
