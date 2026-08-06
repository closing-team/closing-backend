package com.closing.closing.domain.business.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class VerifyBusinessRequest {

    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "사업자등록번호는 숫자 10자리여야 합니다.")
    private String businessNumber;

    @NotBlank(message = "대표자명을 입력해주세요.")
    private String ownerName;

    @NotBlank
    @Pattern(regexp = "\\d{8}", message = "개업일자는 YYYYMMDD 형식이어야 합니다.")
    private String openDate;
}
