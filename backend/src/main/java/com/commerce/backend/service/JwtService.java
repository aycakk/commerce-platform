package com.commerce.backend.service;

import com.commerce.backend.domain.User;
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

    // Ayar dosyasındaki app.jwt.* değerlerini @Value ile buraya alıyoruz
    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // Gizli metni, imzalamaya uygun bir anahtara çeviriyoruz
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Bir kullanıcı için JWT üretir (xxx.yyy.zzz)
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        // Rolleri "USER,ADMIN" gibi tek metne çeviriyoruz
        String roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(user.getEmail())     // PAYLOAD: jetonun sahibi (kim)
                .claim("uid", user.getId())   // PAYLOAD: kullanıcı id
                .claim("roles", roles)        // PAYLOAD: roller
                .issuedAt(now)                // ne zaman üretildi
                .expiration(expiry)           // ne zaman geçersiz olacak
                .signWith(key)                // SIGNATURE: gizli anahtarla imzala
                .compact();                   // hepsini xxx.yyy.zzz metnine çevir
    }
}