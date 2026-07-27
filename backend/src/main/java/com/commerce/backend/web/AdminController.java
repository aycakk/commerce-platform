package com.commerce.backend.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    // Sadece ADMIN rolü olan girebilir.
    // hasRole('ADMIN') -> jetondaki "ROLE_ADMIN" yetkisini arar.
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> summary() {
        return Map.of(
                "mesaj", "Merhaba yönetici! Bu alana sadece ADMIN girebilir.",
                "aktifKullaniciSayisi", 42   // örnek/temsili veri
        );
    }
}
