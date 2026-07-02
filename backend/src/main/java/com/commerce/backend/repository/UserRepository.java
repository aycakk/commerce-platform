package com.commerce.backend.repository;

import com.commerce.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<User, Long>: User entity'si, id tipi Long.
// Bu arayüzden save/findById/findAll/delete gibi metodlar HAZIR gelir.
public interface UserRepository extends JpaRepository<User, Long> {

    // Metot adından sorgu: "email sütununa göre bir kullanıcı bul"
    // Optional: sonuç olabilir de olmayabilir de (null yerine güvenli kutu)
    Optional<User> findByEmail(String email);

    // "Bu email'e sahip kullanıcı var mı?" -> true/false
    boolean existsByEmail(String email);
}
