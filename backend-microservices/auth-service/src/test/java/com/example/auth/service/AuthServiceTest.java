package com.example.auth.service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.exception.ApiException;
import com.example.auth.model.UserAccount;
import com.example.auth.model.UserRole;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtProperties;
import com.example.auth.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;
    @Mock private RedisTemplate<String, String> redisTemplate;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setAccessTokenMinutes(15);
        props.setRefreshTokenDays(7);
        authService = new AuthService(userRepository, refreshTokenRepository, passwordEncoder, jwtProvider, props, redisTemplate);
    }

    @Test
    void shouldThrowWhenEmailExists() {
        when(userRepository.findByEmail("x@test.com")).thenReturn(Optional.of(new UserAccount()));
        assertThrows(ApiException.class, () -> authService.register(new RegisterRequest("x@test.com", "secret", UserRole.USER)));
    }

    @Test
    void shouldThrowForInvalidLogin() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setEmail("x@test.com");
        user.setPasswordHash("hash");
        when(userRepository.findByEmail("x@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(ApiException.class, () -> authService.login(new LoginRequest("x@test.com", "wrong")));
    }
}
