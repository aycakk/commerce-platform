package com.commerce.backend.web;

import com.commerce.backend.service.AuthService;
import com.commerce.backend.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")          // bu sınıftaki tüm yollar /auth ile başlar
public class AuthController {

    private final AuthService authService;

    // Spring, AuthService'i buraya otomatik verir (constructor injection)
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /auth/register
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)   // başarılıysa 201 Created döner
    public void register(@Valid @RequestBody RegisterRequest request) {
        // @Valid: DTO'daki doğrulama kurallarını çalıştır
        // @RequestBody: gelen JSON'u RegisterRequest'e çevir
        authService.register(request);    // işi servise devret
    }
}
