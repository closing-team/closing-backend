package com.closing.closing.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(max = 50, message = "닉네임은 50자 이하로 입력해주세요.")
    private String nickname;

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phone;

    private String email;

    private String profileImageUrl;
}
