package com.closing.closing.domain.support.dto;

import com.closing.closing.domain.support.entity.SupportInfo;
import com.closing.closing.domain.support.entity.SupportStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

public class SupportResDTO {

    @Builder
    public record SupportListDTO(
            List<SupportSummaryDTO> supports,
            PageDTO page
    ) {
    }

    @Builder
    public record SupportDetailDTO(
            Long supportId,
            String organizationName,
            String title,
            String content,

            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate applyStartDate,

            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate applyEndDate,

            String applicationPeriod,
            String externalUrl,
            SupportStatus status,
            boolean isBookmarked,
            int viewCount
    ) {
        public static SupportDetailDTO from(SupportInfo supportInfo, boolean isBookmarked) {
            return SupportDetailDTO.builder()
                    .supportId(supportInfo.getId())
                    .organizationName(supportInfo.getOrganizationName())
                    .title(supportInfo.getTitle())
                    .content(supportInfo.getContent())
                    .applyStartDate(supportInfo.getApplyStartDate())
                    .applyEndDate(supportInfo.getApplyEndDate())
                    .applicationPeriod(supportInfo.getApplicationPeriod())
                    .externalUrl(supportInfo.getExternalUrl())
                    .status(supportInfo.getStatus())
                    .isBookmarked(isBookmarked)
                    .viewCount(supportInfo.getViewCount())
                    .build();
        }
    }

    @Builder
    public record SupportSummaryDTO(
            Long supportId,
            String organizationName,
            String title,

            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate applyStartDate,

            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate applyEndDate,

            String applicationPeriod,
            SupportStatus status,
            boolean isBookmarked,
            int viewCount
    ) {
        public static SupportSummaryDTO from(SupportInfo supportInfo, boolean isBookmarked) {
            return SupportSummaryDTO.builder()
                    .supportId(supportInfo.getId())
                    .organizationName(supportInfo.getOrganizationName())
                    .title(supportInfo.getTitle())
                    .applyStartDate(supportInfo.getApplyStartDate())
                    .applyEndDate(supportInfo.getApplyEndDate())
                    .applicationPeriod(supportInfo.getApplicationPeriod())
                    .status(supportInfo.getStatus())
                    .isBookmarked(isBookmarked)
                    .viewCount(supportInfo.getViewCount())
                    .build();
        }
    }

    @Builder
    public record PageDTO(
            String nextCursor,
            boolean hasNext
    ) {
    }
}
