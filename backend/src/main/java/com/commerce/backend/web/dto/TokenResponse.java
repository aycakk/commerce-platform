package com.commerce.backend.web.dto;

// Login başarılı olunca döneceğimiz cevap: jeton
public record TokenResponse(String accessToken) {
}