package com.closing.closing.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "채팅 메시지 히스토리 커서 페이징 조건")
@Getter
@Setter
@NoArgsConstructor
public class MessageHistoryRequest {

    @Schema(
            description = "다음 페이지 조회용 메시지 ID 커서. 첫 요청에서는 생략하고 직전 응답의 nextCursor를 그대로 전달합니다.",
            example = "105"
    )
    @Positive(message = "커서는 양수여야 합니다.")
    private Long cursor;

    @Schema(
            description = "한 번에 조회할 메시지 개수",
            example = "20",
            defaultValue = "20",
            minimum = "1",
            maximum = "100"
    )
    @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "조회 크기는 100 이하여야 합니다.") // 임시 정책
    @NotNull
    private Integer size = 20;
}
