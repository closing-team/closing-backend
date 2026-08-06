package com.closing.closing.domain.business.dto.response;

import com.closing.closing.domain.business.entity.BusinessRegistration;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@Builder
public class BusinessVerifyResponse {

    private Long registrationId;
    private String businessNumber;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime verifiedAt;

    public static BusinessVerifyResponse from(BusinessRegistration registration) {
        return BusinessVerifyResponse.builder()
                .registrationId(registration.getId())
                .businessNumber(formatBusinessNumber(registration.getBusinessNumber()))
                .verifiedAt(registration.getUpdatedAt().atOffset(ZoneOffset.ofHours(9)))
                .build();
    }

    private static String formatBusinessNumber(String number) {
        return number.substring(0, 3) + "-" + number.substring(3, 5) + "-" + number.substring(5);
    }
}
