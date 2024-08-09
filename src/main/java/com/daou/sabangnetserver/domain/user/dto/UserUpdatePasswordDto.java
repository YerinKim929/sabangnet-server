package com.daou.sabangnetserver.domain.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdatePasswordDto {
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*]).{10,100}$", message = "비밀번호는 10자 이상, 100자 이하의 영소문자/숫자/특수문자를 조합해주세요.")
    String currentPassword;
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*]).{10,100}$", message = "비밀번호는 10자 이상, 100자 이하의 영소문자/숫자/특수문자를 조합해주세요.")
    String newPassword;
}
