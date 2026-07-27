package com.commerce.backend.web.dto;

// Login/refresh başarılı olunca döneceğimiz cevap: iki jeton.
// accessToken: kısa ömürlü, her isteğe eklenir.
// refreshToken: uzun ömürlü, sadece yeni accessToken almak için kullanılır.
public record TokenResponse(String accessToken, String refreshToken) {
}
