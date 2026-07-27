package com.commerce.backend.service;

import com.commerce.backend.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Bir kullanıcı için JWT üretir (xxx.yyy.zzz)
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        String roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    // YENİ: jetonu doğrular ve içindeki bilgileri (claims) döndürür.
    // İmza tutmazsa veya süresi dolmuşsa istisna (exception) fırlatır.
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)              // gizli anahtarla imzayı DOĞRULA
                .build()
                .parseSignedClaims(token)     // jetonu çöz (imza + süre kontrol edilir)
                .getPayload();                // payload'ı (sub, roles, exp...) döndür
    }
}