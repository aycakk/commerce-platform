# Enterprise Commerce Platform — Geliştirme Yol Haritası

> Bu doküman canlı bir yol haritasıdır. Her faz bittiğinde güncelle, kutucukları işaretle, kapsamı ihtiyaca göre kıs. Amaç "her şeyi aynı anda" yapmak değil; bağımlılık sırasına göre ilerleyip her fazda çalışan bir parça çıkarmaktır.

## 0. Gerçekçi Çerçeve

Bu spec tek bir uygulama değil; dört platform (backend, web, Android, iOS) ve yaklaşık 15 işlevsel alan (kimlik, katalog, sepet/sipariş, WMS, ERP, marketplace, ödeme, kargo, CRM, raporlama, AI, admin) içeren tam bir ürün. Tamamı ciddi bir iş yükü. Tek doğru yaklaşım ince bir dikey dilimle başlayıp modül modül büyütmek.

Mutlak hafta vermek yerine her faza relatif boyut (S / M / L / XL) verdim, çünkü gerçek süre haftada kaç saat ayırdığına bağlı. Önemli olan sıra ve her fazın bitiş kriteri.

**Altın kurallar**

* Bir modülü, bağımlı olduğu modül bitmeden başlatma.
* Her faz "Definition of Done" (DoD) ile kapanır; yarım bırakıp atlama yok.
* Önce backend API + Swagger, sonra client. (İstersen client'ı mock veriyle geliştirip API hazır olunca bağlarsın.)
* Her faz sonunda küçük bir demo/checkpoint.

## 1. Mimari Genel Bakış

Backend merkezde, API-first. Tüm client'lar aynı REST API'yi tüketir.

```
                ┌───────────────────────────────────────────────┐
                │                   CLIENTS                      │
                │  Web (React)   Android (Compose)   iOS (Swift) │
                └───────────────────────┬───────────────────────┘
                                        │ REST / JSON + JWT
                                        │ WebSocket (realtime)
                ┌───────────────────────▼───────────────────────┐
                │             BACKEND  (Spring Boot 3)           │
                │   Controllers → Services → Repositories        │
                │   Spring Security (JWT/RBAC) · Validation      │
                └───┬──────────────┬───────────────┬─────────────┘
                    │              │               │
            ┌───────▼──┐    ┌──────▼─────┐   ┌─────▼───────┐
            │PostgreSQL│    │   Redis    │   │Elasticsearch│
            │ (kaynak  │    │ cache /    │   │  arama /    │
            │  doğru)  │    │ session /  │   │  keşif      │
            │ +Flyway  │    │ rate-limit │   │ (PG'den     │
            │          │    │ /stok lock │   │  senkron)   │
            └──────────┘    └────────────┘   └─────────────┘

   Yan servisler: File Storage · Email · Push (FCM/APNs) · Logging/Monitoring
   Altyapı: Docker Compose (lokal) · GitHub Actions (CI/CD)
```

Roller:

* **PostgreSQL** = tek doğruluk kaynağı (source of truth).
* **Redis** = cache, oturum/refresh token, rate limiting, stok rezervasyon kilidi.
* **Elasticsearch** = arama ve keşif; veriyi Postgres'ten event/outbox ile besle (dual-write'tan kaçın).
* **WebSocket** = gerçek zamanlı bildirim ve sipariş durumu.

## 2. MVP Tanımı (İnce Dikey Dilim)

Önce uçtan uca çalışan en küçük parçayı bitir:

**Kimlik → Katalog → Sepet → Checkout (mock ödeme) → Sipariş**

Kapsam:

* **Backend:** register/login + JWT/refresh, ürün listele/detay, sepet, mock ödeme, sipariş oluştur/listele.
* **Altyapı:** Docker Compose (app + postgres + redis), Flyway, Swagger, health check, CI.
* **Client:** tek client (web önerilir; en hızlı geri bildirim). Sayfalar: Home, Listing, Detail, Cart, Checkout, Orders, Auth, Profile.

Bu dilim bittiğinde elinde gösterilebilir bir demo olur. Portföy için bile bu yeterli bir başlangıç; gerisi bunun üstüne eklenir.

## 3. Modül Bağımlılık Haritası

| Modül | Bağımlı olduğu | Faz |
|---|---|---|
| Kimlik & RBAC | (yok) | 1 |
| Katalog (kategori/marka/ürün/varyant) | Kimlik | 2 |
| Sepet & Sipariş + Mock Ödeme | Katalog | 3 |
| İlk Client (Web) | Yukarıdaki API'ler | 4 |
| Kullanıcı Profili & Mağaza | Kimlik, Katalog | 5 |
| Envanter & WMS | Katalog, Sipariş | 6 |
| Ödeme derinliği & Kargo/İade | Sipariş | 7 |
| Marketplace (çok satıcılı) | Mağaza, Sipariş, Ödeme | 8 |
| Arama & Keşif (Elasticsearch) | Katalog | 9 |
| CRM & Müşteri Özellikleri | Sipariş, Kullanıcı | 10 |
| Satınalma (ERP) & Satış | WMS, Tedarikçiler | 11 |
| Admin Panel | Çoğu modülün API'si | 12 |
| Raporlama & Analitik | Sipariş, WMS, CRM verisi | 13 |
| Promosyon (kupon/kampanya) | Sepet/Checkout | 14 |
| AI Özellikleri | Gerçek katalog + kullanım verisi | 15 |
| Mobil (Android + iOS) | Tüm temel API'ler | 16 |
| Sertleştirme & Üretim | Hepsi | 17 |

## 4. Fazlı Plan

Dört etaba böldüm. Her satırın "Tamamlanma Kriteri" o fazın bittiğini gösterir.

### Etap A — Temel & MVP (gösterilebilir ürün)

| Faz | Odak | Tamamlanma Kriteri (DoD) | Boyut |
|---|---|---|---|
| 0 | Repo yapısı, Docker Compose (PG+Redis), Spring Boot iskelet, Flyway, global hata yönetimi, Swagger, health check, CI (build+test) | `docker compose up` ile çalışan boş API + Swagger açılıyor, CI yeşil | M |
| 1 | Register, Login, JWT + Refresh, Logout, parola hashing, temel RBAC (USER/SELLER/ADMIN) | Token alıp korumalı endpoint'e erişebiliyorsun | M |
| 2 | Category (ağaç), Brand, Product, Variant, SKU, görseller (file storage), Tag, Status; liste + detay + temel filtre | Ürün oluştur → listele → detay çalışıyor | L |
| 3 | Cart (ekle/güncelle/sil), Checkout, Mock Payment Gateway, Order + OrderItem, sipariş durum makinesi | Uçtan uca: sepete ekle → checkout → mock ödeme → sipariş oluştu | L |
| 4 | Web client (Vite + Tailwind + shadcn + TanStack Query + Zustand): Home, Listing, Detail, Cart, Checkout, Orders, Auth, Profile | Web'den gerçek API ile uçtan uca alışveriş | L |

➡️ Burada **gösterilebilir MVP** hazır.

### Etap B — Ticaret Derinliği

| Faz | Odak | Tamamlanma Kriteri (DoD) | Boyut |
|---|---|---|---|
| 5 | Profil, Adres, Avatar, Favoriler/Wishlist, bildirim/güvenlik ayarları; Mağaza oluşturma/doğrulama, Seller Dashboard (temel) | Kullanıcı profilini yönetiyor, satıcı mağaza açabiliyor | M |
| 6 | WMS: çoklu depo, raf/göz, stok takibi, **stok rezervasyonu (Redis kilit)**, transfer, hareket geçmişi, düşük stok uyarısı, batch/seri, sayım | Sipariş verince stok rezerve/düşülüyor, hareket geçmişi tutuluyor | XL |
| 7 | Ödeme geçmişi, **iade/geri ödeme**, cüzdan, fatura/fiş; kargo yöntemleri, takip, teslim durumu, iade/değişim talepleri | İade ve kargo durumu uçtan uca çalışıyor | L |
| 8 | Marketplace: çok satıcı, satıcı onayı, satıcı paneli, satıcı cüzdanı, komisyon, payout talepleri | Sipariş geliri komisyonla satıcıya dağıtılıyor, payout açılabiliyor | L |
| 9 | Elasticsearch kurulumu + PG→ES senkron; arama, akıllı filtre, sıralama, ürün karşılaştırma, son görüntülenenler | ES üzerinden hızlı arama + faceted filtre | L |

### Etap C — Genişletme (CRM / ERP / Admin / Raporlama / AI)

| Faz | Odak | Tamamlanma Kriteri (DoD) | Boyut |
|---|---|---|---|
| 10 | Yorum, puan, Soru&Cevap; **WebSocket gerçek zamanlı bildirim**; CRM: müşteri yönetimi/notları, segmentasyon, sadakat puanı, destek talepleri | Yorum/puan, canlı bildirim ve destek talebi çalışıyor | L |
| 11 | ERP Satınalma: tedarikçiler, satınalma talebi/siparişi, mal kabul, tedarikçi faturası/performansı; Satış: teklif, satış siparişi, satış raporları | Tedarikçiden mal girişi stoğa yansıyor | L |
| 12 | Admin Panel (rol bazlı): kullanıcı/satıcı/ürün/kategori/marka/depo/envanter/sipariş/ödeme/kupon yönetimi, audit log görüntüleme, sistem ayarları | Admin tüm temel varlıkları yönetebiliyor | L |
| 13 | Raporlar: dashboard, satış/gelir/envanter/depo/müşteri/satıcı raporları, ürün performansı | Tarih aralığıyla rapor çekilebiliyor | M |
| 14 | Promosyon: kupon, kampanya, indirim kuralları; checkout'a entegrasyon (istersen daha erkene alabilirsin) | Kupon uygula → fiyat düşüyor | M |
| 15 | AI: ürün arama, öneri, açıklama üretici, yorum özetleme, alışveriş asistanı, talep/stok tahmini | En az 2-3 AI özelliği canlı veriyle çalışıyor | L |

### Etap D — Çok Platform & Üretim Hazırlığı

| Faz | Odak | Tamamlanma Kriteri (DoD) | Boyut |
|---|---|---|---|
| 16 | Android (Kotlin/Compose/Hilt/Room/Retrofit/DataStore/FCM) ve iOS (Swift/SwiftUI/SwiftData/URLSession/Keychain): Auth, Home, Catalog, Search, Cart, Checkout, Orders, Notifications, Wishlist, Profile; push (FCM/APNs) | Mobilden uçtan uca alışveriş + push bildirim | XL |
| 17 | Email doğrulama/şifre sıfırlama, Google login, 2FA, ince RBAC/izin yönetimi, rate limiting (Redis), logging/monitoring, audit log (sistem geneli), kapsamlı test (unit+integration), API doküman cilası, CI/CD deploy | Güvenlik + gözlemlenebilirlik + test kapsamı üretim seviyesinde | L |

## 5. Çekirdek Veri Modeli (MVP Etabı)

İlk etapta gereken çekirdek varlıklar ve ana ilişkiler:

* **User** (id, email, passwordHash, status) ─< **UserRole** >─ **Role** (USER / SELLER / ADMIN)
* **Category** (id, name, parentId → self) ağaç yapısı
* **Brand** (id, name)
* **Product** (id, categoryId, brandId, name, description, status) 1─< **ProductVariant** (id, productId, sku, price, attributes)
* **Product** 1─< **ProductImage** (id, productId, url)
* **Cart** (id, userId) 1─< **CartItem** (id, cartId, variantId, qty)
* **Order** (id, userId, status, total) 1─< **OrderItem** (id, orderId, variantId, qty, unitPrice)
* **Payment** (id, orderId, provider=MOCK, status, amount)

Not: Tüm parasal alanlar `BigDecimal` + para birimi. Stok alanı Faz 6'da WMS'e taşınınca varyanttan ayrı tutulur.

## 6. Teknoloji Kararları & Notlar

* **Repo yapısı:** Monorepo öneririm (`backend/`, `web/`, `android/`, `ios/`, `infra/`, `docs/`). Tek yerden yönetim, ortak dokümantasyon. Alternatif: backend ayrı repo, client'lar ayrı.
* **Auth:** Kısa ömürlü JWT access + refresh token (Redis ya da DB'de, rotation ile). Logout'ta refresh'i geçersiz kıl.
* **Migration:** Her şema değişikliği Flyway ile. Elle DB değişikliği yok.
* **Mock ödeme:** Gerçek bir `PaymentProvider` arayüzü tanımla, `MockPaymentProvider` ile uygula. Sonra iyzico/Stripe eklemek tek implementasyon meselesi olur.
* **ES senkron:** Önce PG'ye yaz, sonra event/outbox ile ES'e indexle. "Dual write" tuzağına düşme.
* **Test:** Kritik akışlar (auth, checkout, stok rezervasyonu) integration testle korunsun ve CI'da koşsun.
* **Branching:** Kısa ömürlü feature branch + PR ya da trunk-based. Her PR'da CI yeşil olmadan merge yok.

## 7. Kapsam Disiplini (Scope Creep'e Karşı)

* AI'ı gerçek veri/kullanım birikmeden yapma (Faz 15 sebebi bu).
* Marketplace'i, tek satıcılı sipariş akışı oturmadan açma (Faz 8).
* Admin paneli, yöneteceği API'ler hazır olmadan başlama (Faz 12).
* Her faz bir checkpoint demo'suyla kapanır; "neredeyse bitti" diye bırakıp diğerine geçme.
* Spec'teki bir özelliği görünce "şimdi mi?" diye sor: bağımlılık haritasında yeri neresiyse orada yap.

## 8. Önerilen İlk Somut Adım

**Faz 0 + Faz 1'i birlikte kur:** Docker Compose (Postgres + Redis) + Spring Boot iskelet + Flyway + Swagger + temel auth (register/login/JWT/refresh) + CI. Bu, üstüne her şeyin oturacağı sağlam zemin.

Hazır olduğunda "Faz 0'ı başlat" de; iskeleti, `docker-compose.yml` dosyasını, ilk Flyway migration'ını ve auth uçlarını kurmaya başlayayım.
