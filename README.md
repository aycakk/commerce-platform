# Enterprise Commerce Platform

API-first, çok platformlu (web · Android · iOS) bir e-ticaret platformu. Backend merkezde; tüm client'lar aynı REST API'yi tüketir. Proje, "her şeyi aynı anda" yapmak yerine **ince bir dikey dilimle (MVP) başlayıp modül modül büyüme** ilkesiyle, bağımlılık sırasına göre fazlara bölünmüştür.

> 📍 Tüm fazlar, bağımlılık haritası ve tamamlanma kriterleri için: **[docs/ROADMAP.md](docs/ROADMAP.md)**

## Mimari

```
        Web (React)   ·   Android (Compose)   ·   iOS (Swift)
                        │  REST/JSON + JWT
                        │  WebSocket (realtime)
                ┌───────▼────────────────────────┐
                │   Backend — Spring Boot 3       │
                │   Controllers → Services → Repos│
                │   Spring Security (JWT/RBAC)     │
                └───┬──────────┬───────────┬──────┘
            PostgreSQL       Redis      Elasticsearch
          (source of truth) (cache/    (arama/keşif,
           + Flyway          session/   PG'den senkron)
                             rate-limit/
                             stok kilidi)
```

- **PostgreSQL** — tek doğruluk kaynağı (Flyway ile migration).
- **Redis** — cache, oturum/refresh token, rate limiting, stok rezervasyon kilidi.
- **Elasticsearch** — arama ve keşif; veri Postgres'ten event/outbox ile beslenir.
- **WebSocket** — gerçek zamanlı bildirim ve sipariş durumu.

## Repo Yapısı (monorepo)

| Klasör | İçerik |
|---|---|
| [`backend/`](backend/) | Spring Boot 3 REST API (Security, JPA, Flyway) |
| [`web/`](web/) | React web client (Vite + Tailwind + shadcn + TanStack Query + Zustand) |
| [`android/`](android/) | Android client (Kotlin + Compose + Hilt + Retrofit) |
| [`ios/`](ios/) | iOS client (Swift + SwiftUI + SwiftData) |
| [`infra/`](infra/) | Docker Compose, CI/CD, dağıtım tanımları |
| [`docs/`](docs/) | Dokümantasyon ve yol haritası |

## MVP Kapsamı (İnce Dikey Dilim)

İlk hedef, uçtan uca çalışan en küçük parça:

**Kimlik → Katalog → Sepet → Checkout (mock ödeme) → Sipariş**

- **Backend:** register/login + JWT/refresh, ürün listele/detay, sepet, mock ödeme, sipariş oluştur/listele.
- **Altyapı:** Docker Compose (app + Postgres + Redis), Flyway, Swagger, health check, CI.
- **Web:** Home, Listing, Detail, Cart, Checkout, Orders, Auth, Profile.

## Teknoloji Yığını

| Katman | Teknolojiler |
|---|---|
| Backend | Java · Spring Boot 3 · Spring Security (JWT/RBAC) · Spring Data JPA · Flyway |
| Veri | PostgreSQL · Redis · Elasticsearch |
| Web | Vite · React · Tailwind · shadcn/ui · TanStack Query · Zustand |
| Mobil | Android: Kotlin/Compose/Hilt/Retrofit · iOS: Swift/SwiftUI/SwiftData |
| Altyapı | Docker Compose · GitHub Actions (CI/CD) |

## Yol Haritası — Özet

| Etap | Fazlar | İçerik |
|---|---|---|
| **A — Temel & MVP** | 0–4 | İskelet + auth + katalog + sepet/sipariş + web client → gösterilebilir MVP |
| **B — Ticaret Derinliği** | 5–9 | Profil/Mağaza · WMS · Ödeme/Kargo/İade · Marketplace · Arama (ES) |
| **C — Genişletme** | 10–15 | CRM · ERP · Admin Panel · Raporlama · Promosyon · AI |
| **D — Çok Platform & Üretim** | 16–17 | Android + iOS · güvenlik, test, gözlemlenebilirlik, deploy |

Ayrıntılar ve her fazın "Definition of Done"u → **[docs/ROADMAP.md](docs/ROADMAP.md)**.

## Başlangıç (Faz 0 — yakında)

Faz 0 kurulduğunda lokal geliştirme tek komutla çalışacak:

```bash
cd infra
docker compose up        # app + PostgreSQL + Redis
# Swagger: http://localhost:8080/swagger-ui.html
```

## Geliştirme İlkeleri

- Bir modül, bağımlı olduğu modül bitmeden başlatılmaz.
- Her faz bir "Definition of Done" ve küçük bir demo/checkpoint ile kapanır.
- Önce backend API + Swagger, sonra client.
- Her şema değişikliği Flyway migration ile (elle DB değişikliği yok).
- Her PR'da CI yeşil olmadan merge yok.

## Lisans

Henüz belirlenmedi.
