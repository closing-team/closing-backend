package com.closing.closing.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";
    private static final String TYPE_SIGNUP = "signup";
    private final SecretKey secretKey;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;
    private final long signupTokenExpiry;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry}") long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry,
            @Value("${jwt.signup-token-expiry}") long signupTokenExpiry
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
        this.signupTokenExpiry = signupTokenExpiry;
    }

    public String createAccessToken(Long userId) {
        return buildToken(TYPE_ACCESS, accessTokenExpiry)
                .claim(CLAIM_USER_ID, userId)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        return buildToken(TYPE_REFRESH, refreshTokenExpiry)
                .claim(CLAIM_USER_ID, userId)
                .compact();
    }

    public String createSignupToken(Long userId) {
        return buildToken(TYPE_SIGNUP, signupTokenExpiry)
                .claim(CLAIM_USER_ID, userId)
                .compact();
    }

    public Long getUserId(String token) {
        return getClaims(token).get(CLAIM_USER_ID, Long.class);
    }

    public boolean isSignupToken(String token) {
        return TYPE_SIGNUP.equals(getClaims(token).get(CLAIM_TYPE, String.class));
    }

    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(getClaims(token).get(CLAIM_TYPE, String.class));
    }

    public void validate(String token) {
        try {
            getClaims(token);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("만료된 토큰입니다.");
        } catch (JwtException e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }

    private JwtBuilder buildToken(String type, long expiryMs) {
        Date now = new Date();
        return Jwts.builder()
                .claim(CLAIM_TYPE, type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMs))
                .signWith(secretKey);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
