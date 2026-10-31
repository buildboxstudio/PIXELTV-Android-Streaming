# Idlix TV - Backend API Scraper & Proxy Service

High-performance scraper, cache, and Cloudflare bypass API for **Idlix for TV**.
By running this backend service, the Android TV app does not need any Android `WebView` or Chromium processes, running 100% native Kotlin with minimal memory footprint.

## Fitur Utama
1. **Cloudflare Evasion Server-Side**: Menggunakan stealth browser session untuk melewati Cloudflare Managed / Turnstile Challenge secara otomatis.
2. **In-Memory Caching (LRU)**: Menyimpan cache film, serial, dan kategori di RAM (15-30 menit) sehingga respons ke Android TV instan (<15ms) dan menghemat bandwidth.
3. **All-in-One Playback Resolver**: Menyediakan endpoint `/api/playback/resolve` yang menyelesaikan `gateToken`, countdown, claim session, dan redeem HLS playlist langsung ke master URL siap putar.
4. **Ringan & Hemat RAM**: Image Docker dirancang khusus dengan pembatasan memori (<1GB) dan pemblokiran resource visual (gambar/media/iklan) agar sangat hemat di VPS 1-2GB RAM.

---

## Cara Menjalankan di VPS (Rekomendasi Docker)

1. Clone repo atau upload folder `Backend API` ke VPS Anda:
   ```bash
   cd "Backend API"
   ```

2. Bangun dan jalankan kontainer:
   ```bash
   docker compose up -d --build
   ```

3. Cek log apakah service sudah aktif:
   ```bash
   docker compose logs -f
   ```

4. Uji endpoint:
   ```bash
   curl http://localhost:5376/health
   curl "http://localhost:5376/api/movies?page=1&limit=5"
   ```

---

## Cara Menjalankan Secara Lokal (Node.js)

1. Masuk ke direktori:
   ```bash
   cd "Backend API"
   ```
2. Install dependensi:
   ```bash
   npm install
   ```
3. Jalankan development server:
   ```bash
   npm run dev
   ```
4. Atau bangun untuk produksi:
   ```bash
   npm run build
   npm start
   ```

---

## REST Endpoints Tersedia
- `GET /health` : Status server dan sesi browser
- `GET /api/movies?page=1&limit=20&sort=popularity&genre=slug` : Daftar film
- `GET /api/series?page=1&limit=20&sort=popularity&genre=slug` : Daftar serial TV
- `GET /api/movies/:slug` : Detail film
- `GET /api/series/:slug` : Detail serial beserta episode
- `GET /api/series/:slug/season/:season/episode/:episode` : Detail episode
- `GET /api/genres` : Daftar genre
- `GET /api/search?q=:query` : Pencarian film/serial
- `GET /api/watch/play-info/:contentType/:id` : Gate token provider
- `POST /api/watch/session/claim` : Klaim sesi pemutaran
- `POST /api/watch/redeem` : Redeem master playlist HLS
- `GET /api/playback/resolve?contentType=:type&contentId=:id&contentTitle=:title` : Resolusi tuntas playlist video HLS & subtitle
