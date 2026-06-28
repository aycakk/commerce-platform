# backend

Spring Boot 3 tabanlı, API-first REST backend. Tüm client'lar (web, Android, iOS) bu API'yi tüketir.

**Yığın:** Spring Boot 3 · Spring Security (JWT/RBAC) · PostgreSQL (Flyway migration) · Redis (cache / session / rate-limit / stok kilidi) · Elasticsearch (arama, Faz 9'da).

İskelet Faz 0'da kurulacak: `docker compose up` ile ayağa kalkan boş API + Swagger + health check + CI.
