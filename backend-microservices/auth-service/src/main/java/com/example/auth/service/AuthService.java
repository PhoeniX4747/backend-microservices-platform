package com.example.auth.service;

import com.example.auth.dto.*;
import com.example.auth.exception.ApiException;
import com.example.auth.model.RefreshToken;
import com.example.auth.model.UserAccount;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtProperties;
import com.example.auth.security.JwtProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, String> redisTemplate;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider,
                       JwtProperties jwtProperties,
                       RedisTemplate<String, String> redisTemplate) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ApiException("Email already exists");
        }
        UserAccount user = new UserAccount();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        UserAccount saved = userRepository.save(user);
        log.info("Registered user with id={}", saved.getId());
        return issueTokens(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException("Invalid credentials");
        }
        log.info("User login success id={}", user.getId());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = request.refreshToken();
        String redisKey = buildRedisKey(refreshToken);
        String userId = redisTemplate.opsForValue().get(redisKey);

        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            redisTemplate.delete(redisKey);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
        redisTemplate.delete(redisKey);

        return issueTokens(token.getUser());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        String refreshToken = request.refreshToken();
        String redisKey = buildRedisKey(refreshToken);

        redisTemplate.delete(redisKey);

        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("Refresh token revoked for user id={}", token.getUser().getId());
        });
    }

    private AuthResponse issueTokens(UserAccount user) {
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshTokenValue = java.util.UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(Instant.now().plus(jwtProperties.getRefreshTokenDays(), ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);
        redisTemplate.opsForValue().set(
                buildRedisKey(refreshTokenValue),
                user.getId().toString(),
                jwtProperties.getRefreshTokenDays(),
                TimeUnit.DAYS
        );

        return new AuthResponse(accessToken, refreshTokenValue, jwtProperties.getAccessTokenMinutes() * 60);
    }

    private String buildRedisKey(String refreshToken) {
        return "refresh:" + refreshToken;
    }
}
