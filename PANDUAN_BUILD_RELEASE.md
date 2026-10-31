# 🔨 Panduan Build IDLIX TV — APK Release

Panduan build `app-release.apk` dari proyek `idlix-androidtv`. Ditulis untuk Windows + PowerShell (sesuai environment proyek ini).

---

## 1. Prasyarat

| Kebutuhan | Nilai di mesin ini |
| :--- | :--- |
| **JDK 21** (wajib — proyek pakai `sourceCompatibility = VERSION_21`) | `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot` |
| **Android SDK** | `E:\Projek Aplikasi\android-sdk` (sudah diatur di `local.properties`) |
| **Gradle Wrapper** | `gradle-8.11.1` (sudah termasuk, pakai `gradlew.bat`) |
| **Signing** | ❗ Belum ada — dibuat di Langkah 2 |

> ⚠️ **Java 11 di PATH tidak cukup.** Proyek ini butuh JDK 21. Jika build gagal dengan pesan `Unsupported class file major version` atau `requires JVM target 21`, berarti `JAVA_HOME` masih menunjuk JDK 11.

---

## 2. Buat Keystore Release (sekali saja)

Keystore = identitas permanen aplikasi. **Buat sekali, simpan aman, dan pakai terus.** Kalau hilang, kamu tidak bisa update aplikasi di Play Store / di TV yang sudah terpasang.

```powershell
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\keytool.exe" -genkeypair -v `
  -keystore "E:\Projek Aplikasi\idlix-androidtv\idlix-release.jks" `
  -alias idlixtv `
  -keyalg RSA -keysize 2048 -validity 10000 `
  -storetype JKS `
  -dname "CN=IDLIX TV, OU=Mobile, O=IDLIX, L=Jakarta, ST=DKI Jakarta, C=ID"
```

Kamu akan diminta membuat password keystore dan password key (boleh sama). Catat keduanya.

- File keystore (`*.jks`) sudah otomatis di-*ignore* oleh `.gitignore` — aman, jangan sampai ikut ter-commit.
- Simpan password di password manager. Jangan tulis di repo.

---

## 3. Isi `local.properties`

`app/build.gradle.kts` membaca 4 kunci signing + 1 URL backend dari `local.properties`. Tanpa ini, build release **pasti gagal** (default-nya menunjuk path macOS `/Users/abdi/upload-keystore.jks`).

Edit `E:\Projek Aplikasi\idlix-androidtv\local.properties` menjadi:

```properties
sdk.dir=E:/Projek Aplikasi/android-sdk
backendApiUrl=https://URL-BACKEND-PERMANEN-KAMU/
storeFile=E:/Projek Aplikasi/idlix-androidtv/idlix-release.jks
storePassword=PASSWORD_KEYSTORE_KAMU
keyAlias=idlixtv
keyPassword=PASSWORD_KEY_KAMU
```

Catatan:
- Pakai **garis miring `/`** pada path (garis miring terbalik `\` di file `.properties` dianggap karakter escape).
- `backendApiUrl` = alamat Backend API yang dipakai aplikasi. Ganti dari URL tunnel sementara ke URL server permanen sebelum rilis.
- Simpan nilainya sesuai password yang dibuat di Langkah 2.

---

## 4. Build APK Release

Di PowerShell, dari root proyek:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
.\gradlew.bat clean assembleRelease
```

- `JAVA_HOME` di-set per-sesi agar Gradle memakai JDK 21. Agar permanen: `setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"` lalu buka terminal baru.
- Build pertama lebih lama (unduh dependency + R8).

**Hasil:**

```
app\build\outputs\apk\release\app-release.apk
```

(Opsional) Untuk format AAB ke Play Store: `.\gradlew.bat bundleRelease` → `app\build\outputs\bundle\release\app-release.aab`.

---

## 5. Verifikasi Signature

```powershell
& "E:\Projek Aplikasi\android-sdk\build-tools\35.0.0\apksigner.bat" verify --print-certs "app\build\outputs\apk\release\app-release.apk"
```

Harus muncul `Signer #1 certificate DN: ... IDLIX TV` dan `Verified using v1/v2/v3 scheme: true`. Kalau `DOES NOT VERIFY`, signing di Langkah 3 salah.

---

## 6. Install ke Android TV (via ADB)

```powershell
adb connect 192.168.101.5:5555
adb install -r "app\build\outputs\apk\release\app-release.apk"
adb shell monkey -p com.idlix.tv -c android.intent.category.LAUNCHER 1
```

- **Catatan penting:** versi `release` memakai `applicationId` = `com.idlix.tv` sedangkan `debug` = `com.idlix.tv.debug`. Keduanya aplikasi berbeda — tidak bisa saling timpa. Hapus versi debug dulu bila ingin bersih: `adb uninstall com.idlix.tv.debug`.
- Kalau TV sudah punya versi release lama dengan keystore berbeda, install akan gagal `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Uninstall dulu (data hilang) atau pastikan pakai keystore yang sama.

---

## 7. Troubleshooting

| Gejala | Penyebab & Solusi |
| :--- | :--- |
| `Unsupported class file major version` / `requires JVM target 21` | JAVA_HOME masih JDK 11. Set ke JDK 21 (Langkah 4). |
| `Keystore file not found: /Users/abdi/upload-keystore.jks` | `storeFile` belum diisi di `local.properties` (Langkah 3). |
| `Keystore was tampered with, or password was incorrect` | `storePassword`/`keyPassword` salah. Perbaiki di `local.properties`. |
| `SigningConfig "release" is missing required property` | `keyAlias` kosong/tidak cocok dengan alias saat `keytool`. Cek: `keytool -list -keystore idlix-release.jks`. |
| Aplikasi crash tepat setelah dibuka (versi release saja, debug normal) | R8/ProGuard menghapus class yang dipakai refleksi. Tambahkan aturan `-keep` di `app/proguard-rules.pro`. Uji sementara dengan mengubah `isMinifyEnabled = false` di `app/build.gradle.kts`. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Signature beda dari yang terpasang. Uninstall dulu. |

---

## 8. Sebelum Rilis Resmi

Naikkan versi di `app/build.gradle.kts`:

```kotlin
versionCode = 4        // selalu naik tiap rilis
versionName = "1.0.3"  // versi yang tampil ke pengguna
```

Saat ini: `versionCode = 3`, `versionName = "1.0.2"`.
