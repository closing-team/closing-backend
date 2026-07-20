package com.closing.closing.domain.auth.service;

import com.closing.closing.domain.auth.client.KakaoAuthClient;
import com.closing.closing.domain.auth.dto.response.KakaoTokenResponse;
import com.closing.closing.domain.auth.dto.response.KakaoUserInfoResponse;
import com.closing.closing.domain.auth.dto.response.LoginResponse;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.domain.user.repository.UserRepository;
import com.closing.closing.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoAuthClient kakaoAuthClient;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public LoginResponse kakaoLogin(String code) {
        KakaoTokenResponse kakaoToken = kakaoAuthClient.getToken(code);
        KakaoUserInfoResponse userInfo = kakaoAuthClient.getUserInfo(kakaoToken.getAccessToken());

        Optional<User> existingUser = userRepository.findByKakaoId(userInfo.getKakaoId());

        if (existingUser.isEmpty()) {
            String signupToken = jwtProvider.createSignupToken(userInfo.getKakaoId());
            return LoginResponse.ofNewUser(signupToken);
        }

        User user = existingUser.get();
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        return LoginResponse.ofExistingUser(accessToken, refreshToken);
    }
}
