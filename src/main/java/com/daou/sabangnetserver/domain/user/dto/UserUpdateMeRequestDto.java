package com.daou.sabangnetserver.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateMeRequestDto {
    @NotBlank(message = "이름은 필수로 입력해주세요.")
    private String name;

    @NotBlank(message = "이메일은 필수로 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "이메일 형식에 맞게 작성해주십시오.")
    private String email;

    String department;
    String memo;
}
