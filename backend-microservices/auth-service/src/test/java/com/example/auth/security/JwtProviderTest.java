package com.example.auth.security;

import com.example.auth.model.UserAccount;
import com.example.auth.model.UserRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtProviderTest {

    @Test
    void shouldIncludeRoleClaim() {
        JwtProperties props = new JwtProperties();
        props.setIssuer("auth-service");
        props.setAccessTokenMinutes(15);

        JwtProvider provider = new JwtProvider(props);
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@test.com");
        user.setRole(UserRole.ADMIN);

        String token = provider.generateAccessToken(user);
        var jwt = provider.decode(token);

        assertNotNull(token);
        assertEquals("ADMIN", jwt.getClaimAsString("role"));
    }
}
