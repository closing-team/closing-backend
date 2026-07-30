package com.closing.closing.domain.inquiry.dto.response;

import com.closing.closing.domain.inquiry.entity.Inquiry;
import com.closing.closing.domain.inquiry.entity.InquiryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class InquiryResponse {

    private Long inquiryId;
    private String type;
    private String content;
    private List<String> imageUrls;
    private InquiryStatus status;
    private String answer;
    private LocalDateTime answeredAt;
    private LocalDateTime createdAt;

    public static InquiryResponse from(Inquiry inquiry) {
        return InquiryResponse.builder()
                .inquiryId(inquiry.getId())
                .type(inquiry.getType())
                .content(inquiry.getContent())
                .imageUrls(inquiry.getImageUrls())
                .status(inquiry.getStatus())
                .answer(inquiry.getAnswer())
                .answeredAt(inquiry.getAnsweredAt())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
