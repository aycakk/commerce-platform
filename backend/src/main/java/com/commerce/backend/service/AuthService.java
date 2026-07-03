package com.commerce.backend.service;

import com.commerce.backend.domain.Role;
import com.commerce.backend.domain.User;
import com.commerce.backend.repository.RoleRepository;
import com.commerce.backend.repository.UserRepository;
import com.commerce.backend.web.dto.LoginRequest;
import com.commerce.backend.web.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;   // YENİ: jeton üretici

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {
        // 1) Email zaten kayıtlı mı? Kayıtlıysa 409 (çakışma) hatası ver.
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu email zaten kayıtlı");
        }

        // 2) USER rolünü bul (V2 migration'da eklemiştik). Yoksa sunucu hatası.
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "USER rolü bulunamadı"));

        // 3) Yeni kullanıcıyı oluştur; parolayı HASH'leyerek koy.
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.getRoles().add(userRole);

        // 4) Veritabanına kaydet.
        userRepository.save(user);
    }

    // YENİ: giriş yap, doğruysa JWT dön
    public String login(LoginRequest request) {
        // 1) Email'e göre kullanıcıyı bul. Yoksa 401.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Email veya parola hatalı"));

        // 2) Parola doğru mu? BCrypt ile karşılaştır. Yanlışsa 401.
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email veya parola hatalı");
        }

        // 3) Doğruysa jeton üret ve dön.
        return jwtService.generateToken(user);
    }
}