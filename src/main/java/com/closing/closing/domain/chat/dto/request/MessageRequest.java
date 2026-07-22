package com.closing.closing.domain.chat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageRequest {

    // 이미지 전송 시 content가 없기 때문에 NotBlank 검증을 넣지 않는다
    private String content;
}
