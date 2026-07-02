package com.commerce.backend.repository;

import com.commerce.backend.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // "name sütununa göre rol bul" (örn. 'USER')
    Optional<Role> findByName(String name);
}
