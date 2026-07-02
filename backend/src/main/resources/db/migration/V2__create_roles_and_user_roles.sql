-- V2: roller ve kullanıcı-rol ilişkisi (RBAC'ın temeli)

-- Roller tablosu: USER, SELLER, ADMIN gibi tanımlı roller
CREATE TABLE roles (
    id   BIGSERIAL   PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Ara tablo: hangi kullanıcının hangi rolü var (çoka-çok ilişki)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Başlangıç rolleri (uygulama bunlara güvenecek)
INSERT INTO roles (name) VALUES ('USER'), ('SELLER'), ('ADMIN');
