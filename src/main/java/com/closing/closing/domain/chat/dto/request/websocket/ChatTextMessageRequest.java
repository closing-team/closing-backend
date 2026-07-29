package com.closing.closing.domain.chat.dto.request.websocket;

import jakarta.validation.constraints.NotBlank;

public record ChatTextMessageRequest(

    @NotBlank
    String content
) { }
