package com.commerce.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // Java tarafında "passwordHash", veritabanında "password_hash" sütunu
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    // updatable=false: bu değer bir kez yazılır, sonra değişmez
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // Çoka-çok ilişki: user_roles ara tablosu üzerinden rollere bağlanır
    @ManyToMany(fetch = FetchType.EAGER)   // kullanıcıyı çekince rolleri de gelsin
    @JoinTable(
            name = "user_roles",                              // ara tablo
            joinColumns = @JoinColumn(name = "user_id"),      // bu tarafın anahtarı
            inverseJoinColumns = @JoinColumn(name = "role_id") // karşı tarafın anahtarı
    )
    private Set<Role> roles = new HashSet<>();

    // Kayıt ilk kez veritabanına yazılmadan hemen önce çalışır
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
