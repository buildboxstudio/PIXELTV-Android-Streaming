# Panduan Instalasi PIXEL TV di Android TV & Google TV

Panduan ini bagi Anda yang ingin memasang aplikasi **PIXEL TV** di Smart TV, Android TV Box, atau Google TV tanpa perlu keahlian teknis.

---

## Syarat & Persiapan

1. **Perangkat TV**: Android TV, Google TV, atau TV Box (Mi TV Stick, Chromecast with Google TV, Xiaomi Box, RealMe TV Stick, Aqua, Coocaa, TCL, dsb.).
2. **File APK**: `PixelTV.5.0.2.apk` (lihat tab Releases di repo ini).
3. **Koneksi Wi-Fi**: Pastikan TV terhubung ke jaringan rumah.

---

## Wajib: Aktifkan Izin "Sumber Tidak Dikenal"

Android TV membatasi instalasi dari luar Play Store. Ikuti sekali saja:

### Google TV (Chromecast / Sony / TCL):
1. Buka **Settings** > **System** > **About**.
2. Tekan **OK** pada "Android TV OS Build" sebanyak **7 kali** sampai muncul "Anda sekarang developer!".
3. Kembali ke **Apps** > **Security & Restrictions** > **Install unknown apps**.
4. Aktifkan untuk aplikasi File Manager Anda.

### Android TV Standar (Mi TV Stick / Mi Box):
1. **Settings** > **Device Preferences** > **Security & Restrictions**.
2. Aktifkan **Unknown Sources** untuk File Manager Anda.

---

## Cara Pasang (Pilih Salah Satu)

### Cara 1: Via HP — *Paling Praktis* (Tanpa USB)

1. **Di HP dan TV**: Pasang aplikasi **"Send Files to TV"** dari Google Play Store.
2. **Di TV**: Buka aplikasi tersebut, pilih **RECEIVE**.
3. **Di HP**: Buka aplikasi yang sama, pilih **SEND**, lalu pilih file APK PIXEL TV.
4. **Di TV**: Proses transfer berjalan otomatis. Setelah selesai, buka file langsung dan pilih **Install**.

### Cara 2: Via Flashdisk / USB Drive

1. Copy file `PixelTV.5.0.2.apk` dari komputer ke flashdisk.
2. Colok flashdisk ke TV.
3. Buka **File Commander** atau **AnExplorer** di TV.
4. Buka folder flashdisk, klik file APK, pilih **Install**.

### Cara 3: Langsung dari TV via App "Downloader"

1. Pasang **Downloader by AFTVnews** dari Google Play Store.
2. Masukkan link download APK (dari tab Releases repo ini) ke kolom URL.
3. Tekan **Go**, setelah selesai pilih **Install**.

---

## Pertanyaan Umum

### Muncul peringatan "Diblokir oleh Play Protect"?
Klik **"Detail Selengkapnya"** > **"Tetap Instal"**. Aplikasi ini aman, tanpa virus dan tanpa iklan.

### Aplikasi tidak bisa diinstall?
Pastikan Anda **uninstall aplikasi PIXEL TV versi lama** terlebih dahulu. Versi lama menggunakan package berbeda dan tidak bisa di-update otomatis.

### Katalog film kosong setelah install?
Pastikan TV terhubung ke internet. Jika masih kosong, buka **Settings** di sidebar aplikasi > biarkan field server kosong atau isi `https://z2.idlixku.com/` > **Test Connection** > **Save**.

### Subtitle tidak muncul?
Saat film diputar, tekan **D-pad atas/bawah** untuk memunculkan panel kontrol player. Pilih ikon **CC** untuk memilih subtitle atau mencari subtitle alternatif via SubDL.

### ingin menggunakan server sendiri?
Buka **Settings** di sidebar aplikasi > masukkan URL server Anda (format: `https://alamat-server:port/`) > **Test Connection** > **Save & Apply**.
