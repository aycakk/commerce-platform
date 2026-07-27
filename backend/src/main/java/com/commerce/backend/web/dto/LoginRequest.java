package com.commerce.backend.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "email boş olamaz") String email,
        @NotBlank(message = "parola boş olamaz") String password
) {
}