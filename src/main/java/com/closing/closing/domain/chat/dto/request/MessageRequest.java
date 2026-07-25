package com.closing.closing.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "텍스트 채팅 메시지 요청")
@Getter
@Setter
@NoArgsConstructor
public class MessageRequest {

    @Schema(
            description = """
                    전송할 텍스트 내용입니다. 이미지 없이 텍스트를 보낼 때만 전달합니다.
                    텍스트와 이미지를 동시에 보내거나 둘 다 생략할 수 없습니다.
                    """,
            example = "안녕하세요. 아직 판매 중인가요?"
    )
    private String content;
}
