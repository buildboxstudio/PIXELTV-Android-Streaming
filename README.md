# IDLIX for Android TV 🎬📺

<p align="center">
  <img width="460" height="300" src="https://cdn.abdilah.id/uploads/com.idlix.tv-preview.jpeg">
</p>

A modern, high-performance, **100% native Android TV** application for streaming movies and TV series from IDLIX with an intuitive Netflix-style Leanback user interface.

The project is split into two complementary modules:
1. **Android TV Client (`app/`)**: Built entirely in Kotlin, Jetpack Compose for TV (`androidx.tv.material3`), and AndroidX Media3 (ExoPlayer). It contains **zero WebView/Chromium overhead**, keeping the APK lightweight (~9 MB) and highly responsive on entry-level Android TV hardware.
2. **Backend API Scraper & Proxy (`Backend API/`)**: A Fastify TypeScript service running a headless stealth browser to seamlessly bypass Cloudflare challenges, scrape metadata, and resolve HLS master playlists into ready-to-stream endpoints.

---

## 🌟 Fitur Utama (Key Features)

### 📺 Android TV Client
* **100% Native Architecture**: Bebas Chromium dan WebView di dalam APK. Menggunakan hardware decoder bawaan TV (`MediaCodec`) via AndroidX Media3 ExoPlayer.
* **Netflix-Style Leanback UI**:
  * Floating navigation sidebar dengan smooth overlay (tanpa re-layout konten).
  * Billboard Hero dinamis dengan preview sinopsis dan tombol Putar Cepat.
  * Baris Swimlane horizontal untuk Tren, Populer, Serial TV, dan Kategori Genre.
  * Fitur *Lanjutkan Menonton* (Continue Watching) dengan bar progress visual.
* **Token Query Preserving HLS Player**:
  * Mengatasi proteksi CDN Majorplay (Cloudflare token authorization `?t=...`) secara otomatis pada sub-playlist dan chunk segmen video (`.webp`, `.jpg`, `.js`, dll.).
  * Buffer kontrol TV adaptif untuk pemutaran instan tanpa stuttering.
* **Manajemen Subtitle & Integrasi SubDL**:
  * Subtitle internal IDLIX otomatis (Indonesian & English).
  * Integrasi third-party SubDL.com untuk pencarian dan pengunduhan subtitle alternatif langsung dari player.
* **Menu Pengaturan Terintegrasi (In-App Settings)**:
  * Pengaturan alamat Backend API langsung dari layar TV menggunakan remote D-pad.
  * Pengaturan custom API Key SubDL.
  * Tombol Uji Koneksi *real-time* dengan indikator latensi/status HTTP.
  * Penyimpanan otomatis di penyimpanan lokal TV tanpa perlu re-compile APK.
* **Bilingual Localization**:
  * Mendukung penuh Bahasa Indonesia (`id`/`in`) dan English (`en`). Otomatis mengikuti bahasa sistem TV.

### 🚀 Backend API Service
* **Cloudflare Turnstile & Challenge Bypass**: Stealth browser otomatis menyelesaikan proteksi Cloudflare di sisi server.
* **In-Memory LRU Caching**: Respons data instan (<15ms) untuk katalog film, serial, dan kategori.
* **All-in-One Playback Resolver**: Endpoint `/api/playback/resolve` menyelesaikan siklus `gateToken`, countdown timer provider (15 detik), session claim, dan HLS URL redemption dalam satu panggilan.
* **Ringan & Hemat Resource**: Mendukung Docker dengan batas memori <1GB, ideal dijalankan pada VPS murah (1–2GB RAM) atau komputer lokal.

---

## 📁 Struktur Direktori (Project Structure)

```
Idlix for TV/
├── app/                                 # Modul Aplikasi Android TV (Kotlin + Compose)
│   ├── src/main/java/com/idlix/tv/
│   │   ├── data/
│   │   │   ├── api/                     # Retrofit client & API definitions
│   │   │   ├── local/                   # WatchHistoryStore (riwayat tontonan)
│   │   │   ├── model/                   # Data class & JSON serializers
│   │   │   └── repository/              # IdlixRepository & SubdlRepository
│   │   ├── ui/
│   │   │   ├── components/              # TvNavDrawer, TvMovieCard, TvHeroBillboard, dll.
│   │   │   ├── navigation/              # Navigation Host & routes
│   │   │   ├── screens/                 # Home, Movies, Series, Detail, Player, Settings
│   │   │   └── theme/                   # Tema warna & style Leanback
│   │   └── res/                         # Asset visual & string localization (en, id, in)
│   └── build.gradle.kts
│
├── Backend API/                         # Service Backend Scraper & Cache (Node.js/Docker)
│   ├── src/
│   │   ├── engine/                      # Playwright Stealth Scraper & Token Resolver
│   │   ├── routes/                      # REST endpoints Fastify (/api/...)
│   │   └── services/                    # CacheService (LRU memory cache)
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── package.json
│
├── gradle/                              # Gradle wrapper & version catalogs
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── local.properties                     # Konfigurasi lokal Android SDK & API URL (git-ignored)
└── .gitignore                           # Proteksi credential & build junk
```

---

## 🛠️ Panduan Memulai (Getting Started)

### 1. Menjalankan Backend API

Backend API wajib berjalan agar aplikasi Android TV dapat mengambil katalog film dan streaming video.

#### Opsi A: Menggunakan Docker / Podman (Direkomendasikan)
```bash
cd "Backend API"
docker compose up -d --build # atau podman compose up -d --build
```
Cek apakah server sudah aktif:
```bash
curl http://localhost:5376/health
```

#### Opsi B: Menggunakan Node.js Lokal
```bash
cd "Backend API"
npm install
npm run build
npm start
```

---

### 2. Mengatur URL Backend di Android TV

Ada dua cara mudah:

#### Cara 1: Mengatur di file `local.properties` (Sebelum Build APK)
Buka file `local.properties` di root folder proyek:
```properties
sdk.dir=/path/to/android/sdk
backendApiUrl=http://192.168.x.x:5376/
```
*(Ganti `192.168.x.x` dengan IP lokal komputer/VPS yang menjalankan Backend API).*

#### Cara 2: Langsung di Aplikasi TV (Setelah Install)
Buka aplikasi di Android TV -> Pilih menu **Settings (Pengaturan)** di sidebar -> Masukkan IP server Anda -> Klik **Test Connection** -> Klik **Save & Apply**.

---

### 3. Cara Memasang (Install) APK ke Android TV

Tersedia dua opsi pemasangan sesuai kenyamanan Anda:

---

#### 🟢 Opsi 1: Untuk Pengguna Awam (Mudah & Tanpa Koding)

> 📖 **Panduan Lengkap & Bergambar**: Lihat panduan komprehensif di [**PANDUAN_INSTALASI_TV.md**](file:///Users/abdi/Documents/Personal%20Workspace/Idlix%20for%20TV/PANDUAN_INSTALASI_TV.md).

Bagi pengguna yang tidak terbiasa dengan terminal/coding, Anda cukup menyiapkan file `app-release.apk` (unduh dari tab *Releases* atau hasil build) dan ikuti salah satu metode berikut:

1. **Aktifkan Izin Sumber Tak Dikenal di TV**:
   * Masuk ke **Pengaturan TV (Settings)** > **Aplikasi (Apps)** > **Keamanan & Pembatasan (Security & Restrictions)** > Aktifkan izin **Instal Aplikasi Tidak Dikenal (Install unknown apps)** untuk aplikasi File Manager Anda. *(Pada Google TV, klik 7x pada "Android TV OS Build" di menu System > About untuk membuka menu Developer).*
2. **Pilih Metode Pemasangan**:
   * **Lewat HP (Aplikasi *Send Files to TV*)**: Pasang aplikasi *Send Files to TV* di HP dan TV dari Google Play Store. Kirim APK dari HP ke TV via Wi-Fi yang sama, lalu buka file dan pilih **Install**.
   * **Lewat Flashdisk (USB Drive)**: Salin file APK ke Flashdisk dari komputer, colok ke TV, buka aplikasi File Manager (misal: *File Commander* atau *AnExplorer*), lalu klik file APK untuk memasangnya.
3. **Hubungkan ke Server**:
   * Buka aplikasi IDLIX di TV -> Buka menu **Pengaturan (Settings)** di sidebar -> Masukkan IP server Backend Anda -> Tekan **Uji Koneksi** -> Tekan **Simpan & Terapkan**.

---

#### 💻 Opsi 2: Untuk Pengembang / Developer (via ADB)

Pastikan Android TV Anda sudah mengaktifkan **Developer Options** dan **ADB Debugging (Network)**:

1. Hubungkan ADB ke TV melalui Wi-Fi/LAN:
   ```bash
   adb connect 192.168.x.tv:5555
   ```

2. Build APK Release:
   ```bash
   ./gradlew assembleRelease
   ```
   *File output APK:* `app/build/outputs/apk/release/app-release.apk` 

3. Install APK ke TV:
   ```bash
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```

4. Jalankan aplikasi di TV:
   ```bash
   adb shell monkey -p com.idlix.tv -c android.intent.category.LAUNCHER 1
   ```

---

## 📡 REST API Endpoints (Backend)

| Method | Endpoint | Keterangan |
| :--- | :--- | :--- |
| `GET` | `/health` | Status server dan engine scraper |
| `GET` | `/api/movies` | Daftar film (pagination, sort, filter genre) |
| `GET` | `/api/series` | Daftar serial TV |
| `GET` | `/api/movies/:slug` | Detail film lengkap beserta sinopsis & cast |
| `GET` | `/api/series/:slug` | Detail serial beserta daftar episode tiap season |
| `GET` | `/api/search?q=:keyword` | Pencarian film / serial |
| `GET` | `/api/playback/resolve` | One-shot resolver playlist HLS & subtitle siap putar |

---

## 🔒 Keamanan & Lisensi

* File sensitif seperti `local.properties`, sertifikat signing keystore (`*.jks`), dan credential environment dilindungi di [`.gitignore`](file:///Users/abdi/Documents/Personal%20Workspace/Idlix%20for%20TV/.gitignore).
* Proyek ini dibuat untuk keperluan edukasi dan riset arsitektur aplikasi Android TV Native dan streaming HLS.
