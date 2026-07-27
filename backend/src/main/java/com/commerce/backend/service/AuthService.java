package com.commerce.backend.service;

import com.commerce.backend.domain.Role;
import com.commerce.backend.domain.User;
import com.commerce.backend.repository.RoleRepository;
import com.commerce.backend.repository.UserRepository;
import com.commerce.backend.web.dto.LoginRequest;
import com.commerce.backend.web.dto.RefreshRequest;
import com.commerce.backend.web.dto.RegisterRequest;
import com.commerce.backend.web.dto.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;   // YENİ

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
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

    // Giriş yap: doğruysa hem access hem refresh token dön.
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Email veya parola hatalı"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email veya parola hatalı");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.create(user.getEmail());
        return new TokenResponse(accessToken, refreshToken);
    }

    // YENİ: geçerli bir refresh token'la yeni bir jeton çifti al.
    // Rotation: eski refresh token HEMEN iptal edilir, yerine yenisi verilir.
    // Böylece bir refresh token en fazla bir kez kullanılabilir - çalınmışsa
    // gerçek sahibi onu tekrar kullanmaya çalışınca (zaten iptal olduğu için)
    // başarısız olur ve bu durumu fark edebilir.
    public TokenResponse refresh(RefreshRequest request) {
        String email = refreshTokenService.resolveEmail(request.refreshToken());
        if (email == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Refresh token geçersiz veya süresi dolmuş");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Kullanıcı bulunamadı"));

        refreshTokenService.revoke(request.refreshToken());   // eskisini iptal et (rotation)

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = refreshTokenService.create(user.getEmail());
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    // YENİ: çıkış yap - refresh token'ı Redis'ten sil, bir daha kullanılamasın.
    public void logout(RefreshRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }
}
