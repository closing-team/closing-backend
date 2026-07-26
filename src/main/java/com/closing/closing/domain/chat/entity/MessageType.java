package com.closing.closing.domain.chat.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 메시지 유형: TEXT(텍스트 메시지), IMAGE(이미지 메시지)")
public enum MessageType {
    TEXT, IMAGE
}
