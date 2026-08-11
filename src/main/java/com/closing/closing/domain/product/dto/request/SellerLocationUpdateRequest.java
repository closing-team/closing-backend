package com.closing.closing.domain.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "판매자 활동 지역 수정 요청")
@Getter
@NoArgsConstructor
public class SellerLocationUpdateRequest {

    @Schema(
            description = "동 단위의 판매자 활동 지역",
            example = "원흥동",
            minLength = 2,
            maxLength = 50,
            pattern = "^[가-힣0-9·.\\-]+동$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "판매자 활동 지역은 필수입니다.")
    @Size(max = 50, message = "판매자 활동 지역은 50자 이하로 입력해주세요.")
    @Pattern(regexp = "^[가-힣0-9·.\\-]+동$", message = "동 단위의 지역명을 입력해주세요.")
    private String location;
}
