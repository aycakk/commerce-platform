package com.commerce.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity                        // "Bu sınıf bir veritabanı tablosuna karşılık gelir"
@Table(name = "roles")         // hangi tabloya: roles
@Getter                        // Lombok: get metodlarını (getId, getName...) otomatik üretir
@Setter                        // Lombok: set metodlarını otomatik üretir
public class Role {

    @Id                                            // birincil anahtar (satırın kimliği)
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // id'yi veritabanı üretsin (BIGSERIAL)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)  // roles.name sütunu
    private String name;
}
