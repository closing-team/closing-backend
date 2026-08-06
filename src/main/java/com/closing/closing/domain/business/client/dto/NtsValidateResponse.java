package com.closing.closing.domain.business.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NtsValidateResponse {

    @JsonProperty("status_code")
    private String statusCode;

    private List<BusinessData> data;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BusinessData {

        @JsonProperty("b_no")
        private String bNo;

        private String valid;

        private NtsStatus status;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NtsStatus {

        @JsonProperty("b_stt_cd")
        private String bSttCd;

        @JsonProperty("end_dt")
        private String endDt;
    }
}
