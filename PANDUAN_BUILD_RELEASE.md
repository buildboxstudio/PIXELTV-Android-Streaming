# Panduan Build PIXEL TV — APK Release

Panduan build `app-release.apk` dari proyek `pixeltv-androidtv`. Untuk Windows + PowerShell.

---

## 1. Prasyarat

| Kebutuhan | Nilai |
| :--- | :--- |
| **JDK 21** (wajib — proyek pakai `sourceCompatibility = VERSION_21`) | `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot` |
| **Android SDK** | `E:\Projek Aplikasi\android-sdk` (sudah diatur di `local.properties`) |
| **Gradle Wrapper** | `gradle-8.11.1` (sudah termasuk, pakai `gradlew.bat`) |
| **Signing** | belum ada — dibuat di Langkah 2 |

> Java 11 di PATH tidak cukup. Proyek ini butuh JDK 21. Kalau build gagal dengan `Unsupported class file major version`, JAVA_HOME masih menunjuk JDK 11.

---

## 2. Buat Keystore Release (sekali saja)

Keystore = identitas permanen aplikasi. Buat sekali, simpan aman, pakai terus. Kalau hilang, Anda tidak bisa update aplikasi di TV yang sudah terpasang.

```powershell
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\keytool.exe" -genkeypair -v `
  -keystore "E:\Projek Aplikasi\pixeltv-androidtv\idlix-release.jks" `
  -alias idlixtv `
  -keyalg RSA -keysize 2048 -validity 10000 `
  -storetype JKS `
  -dname "CN=PIXEL TV, OU=Mobile, O=PIXEL, L=Jakarta, ST=DKI Jakarta, C=ID"
```

Buat password keystore dan password key (boleh sama). Catat keduanya.

- File keystore (`*.jks`) sudah di-*ignore* oleh `.gitignore` — aman dari accidental commit.
- Simpan password di password manager. Jangan tulis di repo.

---

## 3. Isi `local.properties`

`app/build.gradle.kts` membaca 4 kunci signing + 1 URL backend dari `local.properties`. Tanpa ini, build release pasti gagal.

Edit `E:\Projek Aplikasi\pixeltv-androidtv\local.properties`:

```properties
sdk.dir=E:/Projek Aplikasi/android-sdk
backendApiUrl=https://z2.idlixku.com/
storeFile=E:/Projek Aplikasi/pixeltv-androidtv/idlix-release.jks
storePassword=PASSWORD_KEYSTORE_KAMU
keyAlias=idlixtv
keyPassword=PASSWORD_KEY_KAMU
```

Catatan:
- Path pakai garis miring `/` (backslash `\` di .properties berarti karakter escape).
- `backendApiUrl` adalah server yang dibuild ke dalam APK sebagai default.
- Untuk APK yang langsung jalan tanpa setting: pakai `https://z2.idlixku.com/`.

---

## 4. Build APK Release

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
.\gradlew.bat clean assembleRelease
```

- Set `JAVA_HOME` per-sesi. Agar permanen: `setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"` lalu buka terminal baru.
- Build pertama lebih lama (unduh dependency + R8).

**Output:**
```
app\build\outputs\apk\release\app-release.apk
```

Opsional AAB ke Play Store: `.\gradlew.bat bundleRelease` → `app\build\outputs\bundle\release\app-release.aab`.

---

## 5. Verifikasi Signature

```powershell
& "E:\Projek Aplikasi\android-sdk\build-tools\35.0.0\apksigner.bat" verify -v "app\build\outputs\apk\release\app-release.apk"
```

Harus muncul `Verified using v2 scheme: true`. Kalau `DOES NOT VERIFY`, signing di Langkah 3 salah.

---

## 6. Install ke Android TV (via ADB)

```powershell
adb connect 192.168.101.5:5555
adb install -r "app\build\outputs\apk\release\app-release.apk"
adb shell monkey -p com.pixeltv.tv -c android.intent.category.LAUNCHER 1
```

Catatan penting:
- Versi `release` pakai `applicationId = com.pixeltv.tv`, versi `debug = com.pixeltv.tv.debug` — berbeda, tidak bisa saling timpa. Hapus debug dulu kalau ada: `adb uninstall com.pixeltv.tv.debug`.
- Kalau TV sudah terinstall versi lama dengan keystore berbeda, install akan gagal `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Uninstall dulu (data hilang) atau pastikan keystore sama.

---

## 7. Troubleshooting

| Gejala | Penyebab & Solusi |
| :--- | :--- |
| `Unsupported class file major version` / `requires JVM target 21` | JAVA_HOME masih JDK 11. Set ke JDK 21 (Langkah 4). |
| `Keystore file not found: /Users/...` | `storeFile` belum diisi di `local.properties` (Langkah 3). |
| `Keystore was tampered with, or password was incorrect` | `storePassword`/`keyPassword` salah di `local.properties`. |
| `SigningConfig "release" is missing required property` | `keyAlias` kosong/tidak cocok. Cek: `keytool -list -keystore idlix-release.jks`. |
| Aplikasi crash setelah dibuka (release, debug normal) | R8 menghapus class yang dipakai refleksi. Tambahkan `-keep` di `app/proguard-rules.pro`, atau sementara ubah `isMinifyEnabled = false` di `app/build.gradle.kts`. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Signature beda dari yang terpasang. Uninstall dulu. |

---

## 8. Sebelum Rilis

Naikkan versi di `app/build.gradle.kts`:

```kotlin
versionCode = 13        // naik tiap rilis
versionName = "5.0.3"  // versi tampilan
```

Saat ini: `versionCode = 12`, `versionName = "5.0.2"`.
