package com.commerce.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity   // @PreAuthorize gibi metot-seviyesi yetki etiketlerini açar (RBAC)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // Jeton kullandığımız için sunucuda oturum TUTMUYORUZ (stateless)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Herkese açık uçlar (jeton gerekmez):
                        .requestMatchers("/auth/**", "/ping", "/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/health").permitAll()
                        // Geri kalan HER ŞEY geçerli jeton ister:
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        // Kimlik YOK (jetonsuz/bozuk) -> 401 Unauthorized
                        .authenticationEntryPoint((request, response, authEx) ->
                                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Kimlik doğrulaması gerekli"))
                        // Kimlik VAR ama yetki YOK (örn. USER, ADMIN'lik yere giriyor) -> 403 Forbidden
                        .accessDeniedHandler((request, response, deniedEx) ->
                                response.sendError(HttpStatus.FORBIDDEN.value(), "Bu işlem için yetkiniz yok")))
                // Kendi JWT filtremizi Spring'in giriş filtresinden ÖNCE çalıştır
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
