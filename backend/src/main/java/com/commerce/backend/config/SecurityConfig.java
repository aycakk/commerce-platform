package com.commerce.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration   // "Bu sınıf ayar (bean tanımı) içerir"
public class SecurityConfig {

    // Parola hash aracı. Uygulamanın her yerinde bunu enjekte edip kullanacağız.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Güvenlik zinciri: gelen her isteğin nasıl karşılanacağını belirler.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST/JSON API olduğumuz için CSRF korumasını kapatıyoruz
                // (CSRF daha çok tarayıcı-form tabanlı oturumlar için gerekli).
                .csrf(csrf -> csrf.disable())
                // ŞİMDİLİK: her isteğe izin ver. JWT hazır olunca burayı sıkacağız.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
