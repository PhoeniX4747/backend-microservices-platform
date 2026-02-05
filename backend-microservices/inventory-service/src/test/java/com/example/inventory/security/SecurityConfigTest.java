package com.example.inventory.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityConfigTest {

    @Test
    void shouldMapAdminRole() {
        SecurityConfig config = new SecurityConfig();
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), Map.of("sub", "1", "role", "ADMIN"));
        var auth = config.jwtAuthenticationConverter().convert(jwt);
        assertEquals("ROLE_ADMIN", auth.getAuthorities().iterator().next().getAuthority());
    }
}
