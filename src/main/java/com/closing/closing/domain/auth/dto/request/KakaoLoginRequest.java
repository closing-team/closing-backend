package com.closing.closing.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class KakaoLoginRequest {

    @NotBlank(message = "카카오 인가 코드가 필요합니다.")
    private String code;
}
