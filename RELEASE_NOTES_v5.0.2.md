# Release Notes — Pixel TV v5.0.2

> **Tanggal**: 30 Oktober 2026
> **Package**: `com.pixeltv.tv`
> **minSdk**: 26 (Android 7.0)
> **targetSdk**: 35 (Android 15)
> **Version Code**: 12
> **SHA-256**: `8BF7E56B2645126E1A1FF9AB03C6DA88CFE1A5C0FD3011C7BF19712B5E5C4C72`

---

## Apa yang Baru

### native, tanpa WebView

v5.0.2 adalah **rewrite total** dari arsitektur WebView ke 100% native Android TV. Tidak adaChromium, tidak ada WebView, tidak ada overhead. APLogging dilakukan dengan Kotlin + Jetpack Compose (`androidx.tv.material3`) + AndroidX Media3 ExoPlayer. Hasilnya: APK ~4 MB, ringan di TV box kelas bawah.

### UI Netflix-style Leanback

- **Floating sidebar** navigasi (overlay halus, tanpa relayout konten)
- **Billboard Hero** dinamis dengan sinopsis dan tombol Putar Cepat
- **Swimlane horizontal**: Tren, Populer, Serial TV, Genre
- **Lanjutkan Menonton** dengan bar progress visual

### Pemutaran Video

- **Media3 ExoPlayer** native — hardware decoder TV, bukan software
- **HLS Token Preserving** — automatis tangani proteksi CDN Majorplay (`?t=...`) di sub-playlist dan chunk video
- **Auto-next episode** untuk serial (tayang 90 detik terakhir)
- **Skip intro** D-pad
- **Subtitle bawaan IDLIX** (Indonesia/English) + pencarian subtitle alternatif via **SubDL.com**

### D-pad Friendly

Dirancang untuk remote TV biasa. Setiap elemen UI bisa dinavigasi dengan D-pad tanpa sentuh.

---

## Syarat Perangkat

| Komponen | Minimum |
|---|---|
| Android TV / Google TV / TV Box | Android 7.0 (API 26) atau lebih baru |
| Ruang penyimpanan | ~15 MB |
| RAM | 1 GB atau lebih |
| Koneksi internet |高速 (untuk streaming HLS) |

Kompatibel dengan: Mi TV Stick, Chromecast with Google TV, Xiaomi Box, RealMe TV Stick, Aqua, Coocaa, TCL, dan perangkat Android TV/Google TV lainnya.

---

## Cara Install

### 1. Hapus Versi Lama

> ⚠️ **Wajib**: Aplikasi ini memiliki package berbeda (`com.pixeltv.tv`) dari versi lama (`com.idlix.tv`). **Uninstall aplikasi lama terlebih dahulu**, kalau tidak install akan gagal.

### 2. Aktifkan Izin "Sumber Tidak Dikenal"

1. Buka **Settings** TV > **Apps** > **Security & Restrictions**
2. Aktifkan **Install unknown apps** untuk aplikasi File Manager Anda

### 3. Pilih Metode Pemasangan

**Via Flashdisk:**
1. Salin file `PixelTV.5.0.2.apk` ke flashdisk
2. Colok ke TV, buka File Manager
3. Klik file APK > **Install**

**Via HP (Send Files to TV):**
1. Install *Send Files to TV* di HP dan TV (Google Play Store)
2. Pastikan HP dan TV di satu jaringan Wi-Fi yang sama
3. Kirim APK dari HP ke TV, buka dan install

### 4. Selesai

Buka aplikasi dari layar utama TV. Katalog film dan serial langsung tersedia — tanpa konfigurasi tambahan.

---

## Catatan Penting

- **Versi lama harus di-uninstall** sebelum install v5.0.2 (beda package).
- Jika TV sudah terinstall versi debug (`com.pixeltv.tv.debug`), uninstall dulu.
- Aplikasi langsung jalan setelah install — server IDLIX sudah terkonfigurasi.
- Untuk menggunakan server sendiri, buka **Settings** di sidebar, masukkan URL server, tekan **Test Connection** > **Save**.

---

## Troubleshooting

| Masalah | Solusi |
|---|---|
| "App not installed" | Uninstall versi lama atau versi debug dulu |
| Katalog kosong | Cek koneksi internet. Atau buka Settings > masukkan URL server > Test > Save |
| Subtitle tidak muncul | Tekan D-pad atas/bawah saat playback > pilih ikon CC |
| Player buffering | Cek kecepatan internet; coba skip forward sedikit untuk buffer ahead |

---

## MD5 Checksum

```
MD5: (cek di release page)
SHA-256: 8BF7E56B2645126E1A1FF9AB03C6DA88CFE1A5C0FD3011C7BF19712B5E5C4C72
```

File: `PixelTV.5.0.2.apk` (4.160.340 byte)

---

## Ringkasan English

PIXEL TV v5.0.2 is a fully native Android TV streaming app built with Kotlin + Jetpack Compose + Media3 ExoPlayer. It offers a Netflix-style leanback UI, HLS playback with CDN token preservation, bilingual subtitles, and D-pad navigation. Requires Android 7.0+ (API 26). Uninstall previous versions before installing due to a different application ID.
