# PIXEL TV Android TV Streaming v5.0.3

[![image_2026-10-31_115353562.png](https://s8d6.turboimg.net/sp/f6cce1746640adf912a6ba4f9913750d/image_2026-10-31_115353562.png)](https://www.turboimagehost.com/p/131208537/image_2026-10-31_115353562.png.html)

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

## Memulai

### Unduh versi terbaru

Unduh **`PixelTV.5.0.2.apk`** dari [GitHub Releases](https://github.com/buildboxstudio/PIXELTV-Android-Streaming/releases/latest). Untuk pemasangan pertama v5.x, uninstall versi lama terlebih dahulu karena package aplikasi berubah.

```
### Pasang APK ke Android TV

#### Untuk Pengguna Biasa
1. Aktifkan **Sumber Tidak Dikenal** di TV (Settings > Apps > Security > Install unknown apps).
2. Salin file APK ke flashdisk atau kirim via *Send Files to TV*.
3. Buka file APK di TV, pilih **Install**.
```
---

## Build dari Source

Lihat [PANDUAN_BUILD_RELEASE.md](PANDUAN_BUILD_RELEASE.md) untuk prasyarat JDK 21, konfigurasi keystore dan `local.properties`, build release, serta verifikasi signature.

---

## Keamanan

`local.properties`, keystore (`*.jks`), dan credential environment dilindungi oleh `.gitignore`. Jangan pernah masukkan password atau secret ke dalam repo.

---

## Summary (English)

PIXEL TV is a native Android TV streaming app for IDLIX, built with Kotlin + Jetpack Compose (`androidx.tv.material3`) + Media3 ExoPlayer. It is a complete rewrite from the ground up ΓÇö no WebView, no Chromium, ~4 MB APK. The accompanying Backend API handles Cloudflare bypass, content scraping, and HLS playlist resolution. Compatible with Android TV, Google TV, Android TV boxes, and any device running Android 7.0 (API 26) or higher. D-pad remote fully supported.
