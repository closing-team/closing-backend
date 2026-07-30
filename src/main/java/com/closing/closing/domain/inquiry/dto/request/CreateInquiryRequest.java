package com.closing.closing.domain.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateInquiryRequest {

    @NotBlank(message = "문의 유형을 입력해주세요.")
    private String type;

    @NotBlank(message = "문의 내용을 입력해주세요.")
    private String content;

    private List<String> imageUrls;
}
