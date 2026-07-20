package com.closing.closing.domain.terms.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class AgreeTermsRequest {

    @NotEmpty(message = "동의할 약관 항목이 없습니다.")
    private List<Long> termIds;
}
