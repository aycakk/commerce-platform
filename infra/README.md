# infra

Altyapı ve çalıştırma tanımları.

- **`docker-compose.yml`** (Faz 0): app + PostgreSQL + Redis (sonra Elasticsearch). Lokal geliştirme tek komutla: `docker compose up`.
- **CI/CD:** GitHub Actions (`.github/workflows/`) — her PR'da build + test.
- İleride: dağıtım (deploy) ve ortam değişkeni şablonları.
