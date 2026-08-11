package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "판매자 활동 지역 수정 응답")
@Getter
@RequiredArgsConstructor
public class SellerLocationResponse {

    @Schema(
            description = "변경된 판매자 활동 지역",
            example = "원흥동",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String location;

    public static SellerLocationResponse from(User user) {
        return new SellerLocationResponse(user.getLocation());
    }
}
