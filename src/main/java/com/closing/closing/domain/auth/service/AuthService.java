package com.closing.closing.domain.auth.service;

import com.closing.closing.domain.auth.client.KakaoAuthClient;
import com.closing.closing.domain.auth.dto.request.SignupRequest;
import com.closing.closing.domain.auth.dto.response.KakaoTokenResponse;
import com.closing.closing.domain.auth.dto.response.KakaoUserInfoResponse;
import com.closing.closing.domain.auth.dto.response.LoginResponse;
import com.closing.closing.domain.auth.dto.response.SignupResponse;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.domain.user.repository.UserRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
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
            // 신규 유저: 카카오 기본 정보로 DB에 먼저 저장 후 signupToken 발급
            // 이름·전화번호·약관 동의는 이후 회원가입 플로우에서 업데이트
            User newUser = User.builder()
                    .kakaoId(userInfo.getKakaoId())
                    .nickname(userInfo.getNickname())
                    .email(userInfo.getEmail())
                    .profileImageUrl(userInfo.getProfileImageUrl())
                    .build();
            User savedUser = userRepository.save(newUser);
            String signupToken = jwtProvider.createSignupToken(savedUser.getId());
            return LoginResponse.ofNewUser(signupToken);
        }

        User user = existingUser.get();
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        return LoginResponse.ofExistingUser(accessToken, refreshToken);
    }

    @Transactional
    public SignupResponse signup(String authorizationHeader, SignupRequest request) {
        String token = extractToken(authorizationHeader);
        try {
            jwtProvider.validate(token);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.AUTH_SIGNUP_TOKEN_NOT_FOUND);
        }

        if (!jwtProvider.isSignupToken(token)) {
            throw new CustomException(ErrorCode.AUTH_SIGNUP_TOKEN_NOT_FOUND);
        }

        Long userId = jwtProvider.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_SIGNUP_TOKEN_NOT_FOUND));

        user.completeSignup(
                request.getName(),
                request.getNickname(),
                request.getPhone(),
                request.getEmail(),
                request.getProfileImageUrl()
        );

        return SignupResponse.builder()
                .accessToken(jwtProvider.createAccessToken(user.getId()))
                .refreshToken(jwtProvider.createRefreshToken(user.getId()))
                .build();
    }

    public void logout(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        try {
            jwtProvider.validate(token);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return authorizationHeader.substring(7);
    }
}
