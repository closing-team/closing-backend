package com.closing.closing.domain.business.client;

import com.closing.closing.domain.business.client.dto.NtsValidateResponse;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NtsClient {

    private static final String VALIDATE_URL = "https://api.odcloud.kr/api/nts-businessman/v1/validate";

    private final RestClient restClient = RestClient.create();

    @Value("${nts.service-key}")
    private String serviceKey;

    public NtsValidateResponse validate(String businessNumber, String startDate, String ownerName) {
        Map<String, Object> body = Map.of(
                "businesses", List.of(Map.of(
                        "b_no", businessNumber,
                        "start_dt", startDate,
                        "p_nm", ownerName
                ))
        );

        String url = VALIDATE_URL + "?serviceKey=" + serviceKey + "&returnType=JSON";

        try {
            return restClient.post()
                    .uri(URI.create(url))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(NtsValidateResponse.class);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.BUSINESS_NTS_FAIL);
        }
    }
}
