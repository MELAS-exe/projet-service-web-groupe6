package com.voom.iamservice.infrastructure.persistence.adapter;

import com.voom.iamservice.application.port.out.JwtPort;
import com.voom.iamservice.domain.model.Role;
import com.voom.iamservice.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtAdapter implements JwtPort {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.token-expiration-time}")
    private long expirationTime;

    @Value("${security.jwt.refresh-token-expiration-time}")
    private long refreshExpirationTime;

    @Override
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", user.getRoles());
        extraClaims.put("userId", user.getId());
        return buildToken(extraClaims, user.getPhoneNumber(), expirationTime);
    }

    @Override
    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user.getPhoneNumber(), refreshExpirationTime);
    }

    @Override
    public String extractPhoneNumber(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    @Override
    public List<String> extractRoles(String token) {
        @SuppressWarnings("unchecked")
        List<String> roles = extractClaim(token, claims -> claims.get("roles", List.class));
        return roles;
    }

    @Override
    public boolean isTokenValid(String token, User user) {
        final String phoneNumber = extractPhoneNumber(token);
        return (phoneNumber.equals(user.getPhoneNumber())) && !isTokenExpired(token);
    }

    //Private helpers

    private String buildToken(Map<String, Object> extraclaims, String subject, long expirationTime) {
        return Jwts.builder()
                .claims(extraclaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey())
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey()) // Validates the signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        // Decodes the plain text secret into a cryptographic key
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
