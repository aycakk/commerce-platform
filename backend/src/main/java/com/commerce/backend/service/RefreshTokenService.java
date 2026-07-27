package com.commerce.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

// Refresh token'ları YÖNETİR: üretir, doğrular, iptal eder.
// JWT değildir - rastgele bir metindir; sahibi (email) Redis'te tutulur.
// Redis'te tutmamızın sebebi: logout'ta veya rotation'da GERİ ÇAĞIRABİLMEK.
// (JWT kendi kendine geçerlidir, sunucu onu "unutamaz"; bu yüzden refresh
// token için JWT değil, sunucu tarafında saklanan bir yapı kullanıyoruz.)
@Service
public class RefreshTokenService {

    // Redis'te anahtarların önüne eklenen sabit ön ek (başka verilerle karışmasın diye)
    private static final String KEY_PREFIX = "refresh_token:";

    private final StringRedisTemplate redisTemplate;
    private final long expirationMs;

    public RefreshTokenService(StringRedisTemplate redisTemplate,
                               @Value("${app.jwt.refresh-expiration-ms}") long expirationMs) {
        this.redisTemplate = redisTemplate;
        this.expirationMs = expirationMs;
    }

    // Yeni bir refresh token üretir, Redis'e "token -> email" olarak süreli kaydeder.
    public String create(String email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                KEY_PREFIX + token,
                email,
                Duration.ofMillis(expirationMs));   // süre dolunca Redis kendisi siler
        return token;
    }

    // Token geçerliyse sahibinin email'ini döner; geçersiz/süresi dolmuşsa null.
    public String resolveEmail(String token) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + token);
    }

    // Token'ı hemen geçersiz kılar (logout ya da rotation'da eskisini iptal etmek için).
    public void revoke(String token) {
        redisTemplate.delete(KEY_PREFIX + token);
    }
}
