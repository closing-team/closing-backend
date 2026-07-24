package com.closing.closing.domain.auth.client;

import com.closing.closing.domain.auth.dto.response.KakaoTokenResponse;
import com.closing.closing.domain.auth.dto.response.KakaoUserInfoResponse;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class KakaoAuthClient {

    private final RestClient restClient = RestClient.create();

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.token-uri}")
    private String tokenUri;

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public KakaoTokenResponse getToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        try {
            return restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(KakaoTokenResponse.class);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.AUTH_KAKAO);
        }
    }

    public KakaoUserInfoResponse getUserInfo(String kakaoAccessToken) {
        try {
            return restClient.get()
                    .uri(userInfoUri)
                    .header("Authorization", "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .body(KakaoUserInfoResponse.class);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.AUTH_KAKAO);
        }
    }
}
