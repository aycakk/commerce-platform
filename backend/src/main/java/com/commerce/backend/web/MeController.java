package com.commerce.backend.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MeController {

    // GET /me  -> sadece geçerli jetonla erişilir
    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        // authentication'ı JwtAuthFilter doldurdu (jetondan)
        return Map.of(
                "email", authentication.getName(),
                "roles", authentication.getAuthorities()
        );
    }
}