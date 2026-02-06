package com.example.auth.security;

import com.example.auth.model.UserAccount;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Component
public class JwtProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        KeyPair keyPair = createOrGenerateKeyPair(jwtProperties);
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new com.nimbusds.jose.jwk.JWKSet(rsaKey)));
        this.jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    public String generateAccessToken(UserAccount user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtProperties.getAccessTokenMinutes() * 60))
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .claim("email", user.getEmail())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }

    private KeyPair createOrGenerateKeyPair(JwtProperties props) {
        try {
            if (props.getPrivateKey() != null && !props.getPrivateKey().isBlank()
                    && props.getPublicKey() != null && !props.getPublicKey().isBlank()) {
                byte[] privateBytes = Base64.getDecoder().decode(props.getPrivateKey());
                byte[] publicBytes = Base64.getDecoder().decode(props.getPublicKey());
                java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
                RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(privateBytes));
                RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(new java.security.spec.X509EncodedKeySpec(publicBytes));
                return new KeyPair(publicKey, privateKey);
            }
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize RSA keys", ex);
        }
    }
}
