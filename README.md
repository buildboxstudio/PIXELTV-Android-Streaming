# PIXEL TV Android TV Streaming v5.0.4

[![image_2026-10-31_115353562.png](https://s8d6.turboimg.net/sp/f6cce1746640adf912a6ba4f9913750d/image_2026-10-31_115353562.png)](https://www.turboimagehost.com/p/131208537/image_2026-10-31_115353562.png.html)

Aplikasi Android TV native untuk streaming film dan serial dari IDLIX. Dibangun dengan Kotlin, Jetpack Compose (`androidx.tv.material3`), dan AndroidX Media3 (ExoPlayer), dengan ukuran APK sekitar 4 MB yang ringan di hardware TV kelas bawah.

---

## Wajib Login Akun IDLIX Sebelum Memutar Tayangan

Sehubungan dengan pembaruan backend IDLIX dan proteksi Cloudflare Turnstile, pengguna wajib memiliki akun IDLIX (gratis) dan masuk ke akun tersebut sebelum memutar film atau serial TV.

1. Daftar akun IDLIX secara gratis melalui browser HP atau komputer.
2. Buka aplikasi Pixel TV di Android TV, pilih menu **Akun** di sidebar kiri, lalu masuk dengan email atau username dan kata sandi akun IDLIX Anda.
3. Pastikan sesi login tetap aktif sebelum memutar film atau serial TV.
4. Catatan: Login menggunakan akun IDLIX membantu mengurangi gangguan verifikasi Cloudflare Turnstile, tetapi verifikasi bisa tetap muncul sewaktu-waktu dan dapat diselesaikan langsung di layar TV.
5. Privasi: Kredensial dan sesi akun hanya tersimpan secara lokal di perangkat TV Anda, tidak ditanam di dalam APK.

---

## Fitur Utama

- **Native Compose + Media3 ExoPlayer**: Pemutar utama memakai hardware decoder TV (`MediaCodec`) via AndroidX Media3 ExoPlayer (~4 MB). WebView ringan hanya dipakai saat verifikasi Cloudflare Turnstile muncul.
- **Netflix-Style Leanback UI**:
  - Sidebar navigasi floating dengan menu **Akun** (login, lihat status akun, perbarui sesi, keluar, serta dialog ganti sesi saat batas perangkat tercapai).
  - Billboard Hero dinamis dengan sinopsis dan tombol Putar Cepat.
  - Swimlane horizontal untuk Tren, Populer, Serial TV, dan Genre.
  - *Lanjutkan Menonton* dengan bar progress visual.
- **Kontrol Player di Bagian Bawah**: Tombol Play/Pause, Mundur 10d, Maju 10d, Aspect Ratio (Fit, Zoom, Regang), dan pemilih Subtitle kini berada sejajar di baris bawah pemutar media.
- **HLS Token Query Preserving & DNS-over-HTTPS**: Otomatis menjaga token CDN di sub-playlist dan chunk video, ditambah DoH bawaan serta auto-refresh sesi saat token kedaluwarsa.
- **Subtitle + SubDL & Auto-Next Episode**: Subtitle bawaan IDLIX (Indonesia/English), pencarian subtitle alternatif via SubDL.com, dan putar otomatis episode berikutnya.
- **Bilingual & D-pad Full**: Mendukung Bahasa Indonesia dan English mengikuti bahasa sistem TV, dirancang penuh untuk remote TV biasa.

---

## Memulai

### Unduh Versi Terbaru

Unduh **`Pixel.TV.v5.0.4.apk`** dari [GitHub Releases](https://github.com/buildboxstudio/PIXELTV-Android-Streaming/releases/latest).

- **Upgrade dari v5.0.x**: Bisa langsung install menimpa versi sebelumnya (`com.pixeltv.tv`).
- **Dari v2.x atau paket lama** (`com.idlix.tv` / `com.streamtv.app`): Uninstall versi lama terlebih dahulu karena package aplikasi berbeda.

### Pasang APK ke Android TV

1. Aktifkan **Sumber Tidak Dikenal** di TV (`Settings > Apps > Security > Install unknown apps`).
2. Salin file `Pixel.TV.v5.0.4.apk` ke flashdisk atau kirim via *Send Files to TV*.
3. Buka file APK di TV, pilih **Install**, lalu buka aplikasi.
4. Masuk ke menu **Akun** di sidebar kiri dan login menggunakan akun IDLIX gratis Anda sebelum memutar film atau serial TV.

---

## Build dari Source

Lihat [PANDUAN_BUILD_RELEASE.md](PANDUAN_BUILD_RELEASE.md) untuk prasyarat JDK 21, konfigurasi keystore dan `local.properties`, build release, serta verifikasi signature.

---

## Keamanan

`local.properties`, keystore (`*.jks`), dan credential environment dilindungi oleh `.gitignore`. Jangan pernah masukkan password, akun pribadi, atau secret ke dalam repo.

---

## Summary (English)

PIXEL TV v5.0.4 is a native Android TV streaming app for IDLIX built with Kotlin, Jetpack Compose (`androidx.tv.material3`), and AndroidX Media3 ExoPlayer (~4 MB APK). Due to updated IDLIX playback requirements, users must register a free IDLIX account via a phone or computer browser and sign in through the **Account** menu in the left sidebar before playing movies or TV series. Account credentials and session cookies are stored only on the user's TV device and are never embedded in the APK. Compatible with Android TV, Google TV, and TV boxes running Android 8.0 (API 26) or higher with full D-pad remote support.
