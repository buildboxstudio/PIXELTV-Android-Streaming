# Changelog — PIXEL TV

Semua perubahan signifikan didokumentasikan di sini. Format mengikuti [Keep a Changelog](https://keepachangelog.com/id/1.1.0/).

---

## [5.0.2] — 2026-10-30

### Aplikasi Android TV
- **Total rewrite** dari WebView → native Compose TV
- Namespace: `com.pixeltv.tv`, versionCode 12
- Teknologi: Kotlin 2.0.21, Compose BOM 2024.11.00, `androidx.tv.material3` 1.0.0
- Media3 ExoPlayer 1.5.1 untuk playback HLS native
- Netflix-style UI: floating sidebar, hero billboard, swimlane horizontal
- Fitur: Lanjutkan Menonton, auto-next episode, skip intro, subtitle + SubDL
- D-pad full support, bilingual (id/en), gesture-resistant

### Backend API
- Fastify 5 + Puppeteer stealth (Cloudflare bypass)
- In-memory LRU cache 15–30 menit
- Endpoint `/api/playback/resolve` one-shot: gate token → countdown → claim → HLS
- Multi-provider registry (`/src/providers/`)
- Docker + PM2 deployment siap

### Lainnya
- Workflow CI/CD GitHub Actions: build TypeScript di runner → SCP artifact ke VPS → PM2 restart
- Dokumen lengkap: README, PANDUAN_INSTALASI_TV, PANDUAN_BUILD_RELEASE
- Keystore `idlix-release.jks` (id: `idlix-release.jks`, alias `idlixtv`)

---

## [4.0.4] — 2025-10-25
- Koreksi signing config
- Perbaikan build output metadata

## [4.0.3] — 2025-10-25
- Perbaikan path di workflow deploy

## [4.0.2] — 2025-10-25
- Perbaikan workflow trigger condition

## [4.0.1] — 2025-10-25
- Perbaikan deploy artifact script

## [4.0.0] — 2025-10-24
- Branch `clean502` dimulai dari IdlixAndroidTV fork
- Namespace `com.idlix.tv` → `com.pixeltv.tv` (applicationId)
- Penambahan `Backend API/`, workflow GitHub Actions, dokumentasi
- Aplikasi label: "Pixel TV"

---

## [2.2.0] — 2026-06-09

> Catatan: Versi terakhir dari arsitektur lama (WebView).

- Tema Red/Black/Grey
- Perbaikan gesture handling
- Penambahan dukungan situs Nobargasi

## [2.1.0] — 2026-06-01
- Lapisan navigasi JS untuk Android TV remote

## [1.5.1] — 2026-05-13
- Fitur stabil awal

---

> **Catatan**: Aplikasi versi 5.x memiliki `applicationId` berbeda (`com.pixeltv.tv`) dari versi lama (`com.idlix.tv`). Pengguna harus **uninstall versi lama** sebelum install v5.x karena tidak bisa di-update otomatis.
