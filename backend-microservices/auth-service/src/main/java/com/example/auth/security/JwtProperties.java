package com.example.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String issuer;
    private long accessTokenMinutes;
    private long refreshTokenDays;
    private String privateKey;
    private String publicKey;

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public long getAccessTokenMinutes() { return accessTokenMinutes; }
    public void setAccessTokenMinutes(long accessTokenMinutes) { this.accessTokenMinutes = accessTokenMinutes; }
    public long getRefreshTokenDays() { return refreshTokenDays; }
    public void setRefreshTokenDays(long refreshTokenDays) { this.refreshTokenDays = refreshTokenDays; }
    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
}
