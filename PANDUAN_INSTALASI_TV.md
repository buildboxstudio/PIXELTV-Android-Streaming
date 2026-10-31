# 📺 Panduan Instalasi IDLIX di Android TV & Google TV (Untuk Pengguna Awam)

Panduan ini ditujukan bagi Anda yang ingin memasang (**install**) aplikasi IDLIX di Smart TV, Android TV Box, atau Google TV tanpa perlu keahlian teknis atau menggunakan laptop/coding.

---

## 📋 Syarat & Persiapan Awal

Sebelum memulai, siapkan hal-hal berikut:
1. **Perangkat TV**: Android TV, Google TV, atau TV Box (seperti Mi TV Stick, Chromecast with Google TV, Xiaomi Box, RealMe TV Stick, Aqua, Coocaa, TCL, dsb.).
2. **File APK**: File mentah aplikasi IDLIX TV (`app-release.apk` atau `idlix-tv.apk`).
3. **Koneksi Wi-Fi**: Pastikan TV terhubung ke jaringan Wi-Fi rumah Anda.

---

## ⚙️ Langkah Wajib: Mengaktifkan Izin "Sumber Tidak Dikenal"

Android TV secara default membatasi instalasi aplikasi dari luar Google Play Store. Ikuti langkah ini sekali saja:

### Untuk Google TV (Versi Baru / Chromecast / Sony / TCL):
1. Masuk ke **Setelan (Settings)** di pojok kanan atas layar TV (ikon gerigi).
2. Pilih **Sistem (System)** > **Tentang (About)**.
3. Geser ke bawah ke tulisan **Build OS Android TV (Android TV OS Build)**.
4. Tekan tombol **OK / Tengah** di remote sebanyak **7 kali berturut-turut** hingga muncul tulisan *"Anda sekarang adalah developer!"*.
5. Tekan tombol *Back/Kembali*, lalu buka menu **Aplikasi (Apps)** > **Keamanan & Pembatasan (Security & Restrictions)**.
6. Pilih **Instal Aplikasi Tidak Dikenal (Install unknown apps)**.
7. Cari aplikasi File Manager yang akan digunakan (misal: *File Commander*, *Send Files to TV*, atau *AnExplorer*), lalu ubah posisinya menjadi **Aktif / Izinkan (Allow)**.

### Untuk Android TV Standar (Xiaomi Mi TV Stick / Mi Box):
1. Buka **Setelan (Settings)** > **Preferensi Perangkat (Device Preferences)**.
2. Pilih **Keamanan & Pembatasan (Security & Restrictions)**.
3. Aktifkan opsi **Sumber Tidak Dikenal (Unknown Sources)** untuk aplikasi File Manager Anda.

---

## 🚀 Pilihan Cara Pasang (Pilih Salah Satu)

Ada 3 metode mudah yang bisa Anda pilih sesuai kondisi TV Anda:

### 🔹 Cara 1: Menggunakan HP (Paling Praktis & Tanpa Kabel / USB)
*Sangat disarankan untuk Mi TV Stick atau Chromecast yang tidak memiliki lubang colokan USB.*

1. **Install aplikasi perantara di HP & TV**:
   * Buka **Google Play Store** di TV dan di HP Android Anda.
   * Cari dan pasang aplikasi bernama **"Send Files to TV" (SFTTV)** di kedua perangkat tersebut.
   * Pasang juga aplikasi file manager di TV (misalnya **File Commander** atau **AnExplorer** dari Play Store TV).
2. **Kirim APK dari HP ke TV**:
   * Pastikan HP dan TV terhubung ke **jaringan Wi-Fi yang sama**.
   * Buka aplikasi *Send Files to TV* di TV dan pilih **RECEIVE** (Terima).
   * Buka aplikasi *Send Files to TV* di HP, pilih **SEND** (Kirim), lalu pilih file APK IDLIX yang ada di HP Anda.
   * Pilih nama TV Anda di layar HP. Proses transfer akan berjalan dalam beberapa detik.
3. **Instal di TV**:
   * Setelah selesai, klik file tersebut langsung dari layar aplikasi *Send Files to TV* di TV, lalu pilih **Open with Package Installer**.
   * Jika tidak bisa langsung dibuka, buka aplikasi **File Commander / AnExplorer** di TV, masuk ke folder **Download**, lalu klik file APK IDLIX untuk menginstalnya.
   * Pilih **Install**. Selesai!

---

### Cara 2: Menggunakan Flashdisk / USB Drive (Paling Mudah jika ada Port USB)
*Sangat cocok untuk Smart TV atau TV Box yang memiliki lubang USB biasa.*

1. **Siapkan Flashdisk**:
   * Salin (copy) file APK IDLIX dari komputer/laptop ke dalam Flashdisk.
2. **Pasang File Manager di TV**:
   * Buka **Google Play Store** di TV.
   * Cari dan pasang salah satu aplikasi File Manager gratis (contoh: **File Commander**, **FX File Explorer**, atau **AnExplorer**).
3. **Colok Flashdisk ke TV**:
   * Masukkan Flashdisk ke port USB di TV Anda.
4. **Buka dan Pasang**:
   * Buka aplikasi File Manager di TV.
   * Buka drive Flashdisk Anda, cari file APK IDLIX.
   * Tekan tombol **OK** pada file APK, lalu pilih **Install**.
   * Tunggu hingga muncul pemberitahuan *"Aplikasi Terpasang (App installed)"*.

---

### 🔹 Cara 3: Menggunakan Aplikasi "Downloader" (Download Langsung di TV)

1. Buka **Google Play Store** di TV, cari dan pasang aplikasi **Downloader by AFTVnews**.
2. Buka aplikasi **Downloader** di TV.
3. Masukkan link download langsung dari file APK Anda pada kolom URL yang disediakan (misalnya link Google Drive publik atau link rilis GitHub).
4. Tekan tombol **Go**. File akan terunduh langsung ke TV.
5. Setelah unduhan selesai, jendela instalasi otomatis muncul. Pilih **Install**.

---

## ❓ Pertanyaan & Masalah yang Sering Terjadi (Troubleshooting)

### 1. Muncul peringatan "Diblokir oleh Play Protect"?
* **Penyebab**: Karena aplikasi dipasang secara manual (sideload) di luar Play Store resmi.
* **Solusi**: Klik tulisan **"Detail Selengkapnya (More details)"**, lalu pilih **"Tetap Instal (Install anyway)"**. Aplikasi ini 100% aman dan bebas virus/iklan.

### 2. Status "Koneksi Gagal" saat Uji Koneksi di Pengaturan TV?
* **Cek 1**: Pastikan komputer/laptop yang menjalankan Backend API dalam keadaan menyala dan terminal/Docker tidak ditutup.
* **Cek 2**: Pastikan TV dan Komputer terhubung ke **satu jaringan Wi-Fi/router yang sama**.
* **Cek 3**: Jangan gunakan `http://localhost:5376/` di TV, melainkan gunakan IP lokal komputer Anda (contoh: `http://192.168.x.x:5376/`).
* **Cek 4**: Cek firewall di komputer Anda, pastikan port `5376` diizinkan menerima koneksi dari jaringan lokal.

### 3. Subtitle tidak muncul atau ingin ganti bahasa?
* Saat film diputar, tekan tombol arah **Atas/Bawah** di remote TV untuk memunculkan panel kontrol player.
* Pilih tombol **Ikon Subtitle (CC)** untuk memilih subtitle resmi bawaan IDLIX atau mencari subtitle alternatif via integrasi SubDL.

