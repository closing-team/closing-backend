package com.closing.closing.domain.support.client;

import com.closing.closing.domain.support.dto.BizInfoResDTO;
import com.closing.closing.domain.support.exception.SupportException;
import com.closing.closing.domain.support.exception.code.SupportErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BizInfoClient {

    private static final String BASE_URL = "https://www.bizinfo.go.kr";
    private static final String API_PATH = "/uss/rss/bizinfoApi.do";

    private final RestClient restClient;
    private final String apiKey;

    public BizInfoClient(@Value("${BIZINFO_API_KEY:}") String apiKey) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(30_000);

        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(requestFactory)
                .build();
        this.apiKey = apiKey;
    }

    public BizInfoResDTO.BizInfoResponseDTO getSupportAnnouncements(
            int pageSize,
            int pageIndex) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new SupportException(SupportErrorCode.SUPPORT_API_KEY_NOT_FOUND);
        }

        try {
            BizInfoResDTO.BizInfoResponseDTO response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(API_PATH)
                            .queryParam("crtfcKey", apiKey)
                            .queryParam("dataType", "json")
                            .queryParam("pageUnit", pageSize)
                            .queryParam("pageIndex", pageIndex)
                            .build())
                    .retrieve()
                    .body(BizInfoResDTO.BizInfoResponseDTO.class);

            if (response == null || response.jsonArray() == null) {
                throw new SupportException(SupportErrorCode.SUPPORT_EXTERNAL_API_ERROR);
            }
            return response;
        } catch (SupportException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new SupportException(SupportErrorCode.SUPPORT_EXTERNAL_API_ERROR);
        }
    }
}
