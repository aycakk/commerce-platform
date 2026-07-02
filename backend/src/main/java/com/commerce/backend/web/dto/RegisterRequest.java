package com.commerce.backend.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO (Data Transfer Object): dışarıdan gelen JSON'u taşıyan kutu.
// record: sadece veri taşıyan, kısa ve değişmez sınıf türü.
// Anotasyonlar = doğrulama kuralları (Controller'da @Valid ile tetiklenir).
public record RegisterRequest(

        @NotBlank(message = "email boş olamaz")
        @Email(message = "geçerli bir email girin")
        String email,

        @NotBlank(message = "parola boş olamaz")
        @Size(min = 8, max = 100, message = "parola en az 8 karakter olmalı")
        String password
) {
}
