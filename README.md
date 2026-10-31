# PIXEL TV ΓÇö Android TV Streaming

Aplikasi Android TV native untuk streaming film dan serial dari IDLIX. Arsitektur 100% native Kotlin + Jetpack Compose (`androidx.tv.material3`) + AndroidX Media3 (ExoPlayer). Tanpa WebView atau Chromium.

Proyek terdiri dari dua bagian:
1. **Android TV Client (`app/`)**: Kotlin, Compose TV, Media3 ExoPlayer. APK ~4 MB, ringan di hardware kelas bawah.
2. **Backend API (`Backend API/`)**: Fastify TypeScript + Puppeteer stealth ΓÇö bypass Cloudflare, cache, resolve playlist HLS siap putar.

---

## Fitur Utama

### Aplikasi Android TV
- **100% Native**: Nol WebView. Hardware decoder TV (`MediaCodec`) via Media3 ExoPlayer.
- **Netflix-Style Leanback UI**:
  - Sidebar navigasi floating (smooth overlay, tanpa relayout konten).
  - Billboard Hero dinamis dengan sinopsis dan tombol Putar Cepat.
  - Swimlane horizontal: Tren, Populer, Serial TV, Genre.
  - *Lanjutkan Menonton* dengan bar progress visual.
- **HLS Player dengan Token Query Preserving**: Otomatis tangani proteksi CDN Majorplay (`?t=...`) di sub-playlist dan chunk video.
- **Subtitle + SubDL**: Subtitle bawaan IDLIX (Indonesia/English) + pencarian subtitle alternatif via SubDL.com.
- **Bilingual**: Indonesia dan English, mengikuti bahasa sistem TV.
- **D-pad Full**: Didesain untuk remote TV biasa, tanpa sentuh.

### Backend API
- **Cloudflare Bypass**: Stealth browser di server, automatis.
- **In-Memory LRU Cache**: Respons katalog <15ms.
- **One-Shot Playback Resolver**: `/api/playback/resolve` menyelesaikan gate token, countdown, session claim, dan playlist HLS dalam satu panggilan.
- **Ringan**: Docker image <1GB RAM, bisa jalan di VPS 1ΓÇô2GB.

---

## Struktur Direktori

```
pixeltv-androidtv/
Γö£ΓöÇΓöÇ app/                                 # Aplikasi Android TV (Kotlin + Compose)
Γöé   Γö£ΓöÇΓöÇ src/main/java/com/idlix/tv/
Γöé   Γöé   Γö£ΓöÇΓöÇ data/
Γöé   Γöé   Γöé   Γö£ΓöÇΓöÇ api/                     # Retrofit client & API definitions
Γöé   Γöé   Γöé   Γö£ΓöÇΓöÇ local/                   # WatchHistoryStore
Γöé   Γöé   Γöé   Γö£ΓöÇΓöÇ model/                   # Data class & JSON serializers
Γöé   Γöé   Γöé   ΓööΓöÇΓöÇ repository/              # IdlixRepository & SubdlRepository
Γöé   Γöé   Γö£ΓöÇΓöÇ ui/
Γöé   Γöé   Γöé   Γö£ΓöÇΓöÇ components/              # TvNavDrawer, TvMovieCard, TvHeroBillboard
Γöé   Γöé   Γöé   Γö£ΓöÇΓöÇ navigation/              # AppNavigation (drawer + routes)
Γöé   Γöé   Γöé   Γö£ΓöÇΓöÇ screens/                 # Home, Movies, Series, Detail, Player, Settings
Γöé   Γöé   Γöé   ΓööΓöÇΓöÇ theme/                   # Tema warna & style Leanback
Γöé   Γöé   ΓööΓöÇΓöÇ res/                         # Asset visual & localization (en, id, in)
Γöé   ΓööΓöÇΓöÇ build.gradle.kts
Γöé
Γö£ΓöÇΓöÇ Backend API/                         # Service Backend (Node.js / Docker)
Γöé   Γö£ΓöÇΓöÇ src/
Γöé   Γöé   Γö£ΓöÇΓöÇ engine/                      # Puppeteer Stealth Scraper & Token Resolver
Γöé   Γöé   Γö£ΓöÇΓöÇ routes/                      # REST endpoints Fastify (/api/...)
Γöé   Γöé   ΓööΓöÇΓöÇ services/                    # CacheService (LRU memory)
Γöé   Γö£ΓöÇΓöÇ Dockerfile
Γöé   Γö£ΓöÇΓöÇ docker-compose.yml
Γöé   ΓööΓöÇΓöÇ package.json
Γöé
Γö£ΓöÇΓöÇ gradle/                              # Gradle wrapper & version catalogs
Γö£ΓöÇΓöÇ build.gradle.kts
Γö£ΓöÇΓöÇ settings.gradle.kts
Γö£ΓöÇΓöÇ gradle.properties
Γö£ΓöÇΓöÇ local.properties                     # Konfigurasi lokal (git-ignored)
ΓööΓöÇΓöÇ .gitignore
```

---

## Memulai

### Unduh versi terbaru

Unduh **`PixelTV.5.0.2.apk`** dari [GitHub Releases](https://github.com/buildboxstudio/PIXELTV-Android-Streaming/releases/latest). Untuk pemasangan pertama v5.x, uninstall versi lama terlebih dahulu karena package aplikasi berubah.

### 1. Jalankan Backend API

#### Opsi A: Docker (Direkomendasikan)
```bash
cd "Backend API"
docker compose up -d --build
curl http://localhost:5376/health
```

#### Opsi B: Node.js Lokal
```bash
cd "Backend API"
npm install
npm run build
npm start
```

### 2. Pasang APK ke Android TV

#### Untuk Pengguna Biasa
1. Aktifkan **Sumber Tidak Dikenal** di TV (Settings > Apps > Security > Install unknown apps).
2. Salin file APK ke flashdisk atau kirim via *Send Files to TV*.
3. Buka file APK di TV, pilih **Install**.

#### Untuk Developer (via ADB)
```bash
adb connect 192.168.x.x:5555
adb install -r app/build/outputs/apk/release/app-release.apk
adb shell monkey -p com.pixeltv.tv -c android.intent.category.LAUNCHER 1
```

### 3. Konfigurasi Server (Opsional)

APK sudah terkonfigurasi dengan server IDLIX publik. Kalau ingin pakai server sendiri:
- Buka aplikasi > **Settings** di sidebar > masukkan URL server > **Test Connection** > **Save**.

---

## Endpoint REST API

| Method | Endpoint | Keterangan |
| :--- | :--- | :--- |
| `GET` | `/health` | Status server dan engine scraper |
| `GET` | `/api/movies` | Daftar film (pagination, sort, genre) |
| `GET` | `/api/series` | Daftar serial TV |
| `GET` | `/api/movies/:slug` | Detail film + sinopsis + cast |
| `GET` | `/api/series/:slug` | Detail serial + episode per season |
| `GET` | `/api/search?q=:keyword` | Pencarian film / serial |
| `GET` | `/api/playback/resolve` | Playlist HLS + subtitle siap putar |

---

## Build dari Source

Lihat [PANDUAN_BUILD_RELEASE.md](PANDUAN_BUILD_RELEASE.md) untuk prasyarat JDK 21, konfigurasi keystore dan `local.properties`, build release, serta verifikasi signature.

---

## Keamanan

`local.properties`, keystore (`*.jks`), dan credential environment dilindungi oleh `.gitignore`. Jangan pernah masukkan password atau secret ke dalam repo.

---

## Summary (English)

PIXEL TV is a native Android TV streaming app for IDLIX, built with Kotlin + Jetpack Compose (`androidx.tv.material3`) + Media3 ExoPlayer. It is a complete rewrite from the ground up ΓÇö no WebView, no Chromium, ~4 MB APK. The accompanying Backend API handles Cloudflare bypass, content scraping, and HLS playlist resolution. Compatible with Android TV, Google TV, Android TV boxes, and any device running Android 7.0 (API 26) or higher. D-pad remote fully supported.
