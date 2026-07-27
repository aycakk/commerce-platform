package com.commerce.backend.web.dto;

import jakarta.validation.constraints.NotBlank;

// Hem /auth/refresh hem /auth/logout bu kutuyu kullanır: "işte refresh token'ım"
public record RefreshRequest(
        @NotBlank(message = "refreshToken boş olamaz") String refreshToken
) {
}
