package com.commerce.backend.config;

import com.commerce.backend.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // "Authorization: Bearer <jeton>" var mı?
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);   // "Bearer " kısmını at
            try {
                // Jetonu doğrula + içindekileri (claims) oku
                Claims claims = jwtService.parse(token);

                String email = claims.getSubject();
                String rolesStr = claims.get("roles", String.class);

                // "USER,ADMIN" -> Spring'in anladığı yetki listesine çevir
                List<SimpleGrantedAuthority> authorities = Arrays.stream(
                                rolesStr == null ? new String[0] : rolesStr.split(","))
                        .filter(r -> !r.isBlank())
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .toList();

                // "Bu istek sahibi doğrulandı" diye işaretle
                var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                // Jeton geçersiz/süresi dolmuş: işaretleme, akış devam etsin
                // (korumalı endpoint sonra 401 verecek)
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);   // sıradaki adıma geç
    }
}