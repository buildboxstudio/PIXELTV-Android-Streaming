# PIXELTV

PIXELTV - Android TV & Mobile streaming app with built-in ad blocker, multi-site support, bookmark, history, and D-pad navigation. Features draggable floating menu, modern glassmorphism UI, and fullscreen video handler. Built with Kotlin + WebView.

**Version:** 1.5.1  
**Build by:** [Buildbox Studio](https://www.tiktok.com/@buildbox.studio)

---

## 🇬🇧 English

### Features

- ✅ Fullscreen WebView with ad blocker
- ✅ Compatible with Android TV (D-pad native focus navigation)
- ✅ Compatible with Phone/Tablet (touch)
- ✅ Fullscreen video handler
- ✅ Custom User-Agent (desktop browser)
- ✅ Hardware acceleration
- ✅ Keep screen on while streaming
- ✅ Immersive mode (hide status bar & nav bar)
- ✅ Bookmark — save favorite pages
- ✅ History — automatic watch history
- ✅ Multi-site — 3 streaming sites, switch with CH+/CH-
- ✅ Draggable floating button — move anywhere, position saved
- ✅ Modern overlay menu — glassmorphism design with animation
- ✅ Media control — Play/Pause video via remote
- ✅ Anti auto-refresh (fix idle reload)
- ✅ Block popup & ad redirects

### TV Remote Navigation

| Button | Function |
|--------|----------|
| D-pad | Move focus between elements (native WebView) |
| OK / Center | Click focused element |
| Back | Go to previous page |
| Menu | Open overlay menu (bookmark, history, sites) |
| Info/Guide | Quick bookmark current page |
| CH+ / CH- | Switch streaming site |
| Play/Pause | Play/pause video |

### Phone/Tablet Navigation

| Action | Function |
|--------|----------|
| Tap ▶ button (floating) | Open overlay menu |
| Drag ▶ button | Move button position |
| Long press ▶ button | Quick bookmark |
| Touch/scroll | Normal navigation |

### Build

1. Open `StreamTV` folder in **Android Studio**
2. Wait for Gradle sync
3. Run on device/emulator (Android TV or Phone)

### Minimum Requirements

- Android 5.0 (API 21)
- Target: Android 14 (API 34)
- Kotlin 1.9+

---

## 🇮🇩 Bahasa Indonesia

### Fitur

- ✅ WebView fullscreen dengan ad blocker
- ✅ Kompatibel Android TV (navigasi D-pad native)
- ✅ Kompatibel HP/Tablet (touch)
- ✅ Fullscreen video handler
- ✅ Custom User-Agent (desktop browser)
- ✅ Hardware acceleration
- ✅ Layar tetap nyala saat streaming
- ✅ Immersive mode (sembunyikan status bar & nav bar)
- ✅ Bookmark — simpan halaman favorit
- ✅ History — riwayat tontonan otomatis
- ✅ Multi-site — 3 situs streaming, ganti pakai CH+/CH-
- ✅ Floating button draggable — geser ke mana aja, posisi tersimpan
- ✅ Menu overlay modern — desain glassmorphism dengan animasi
- ✅ Media control — Play/Pause video via remote
- ✅ Anti auto-refresh (fix reload saat idle)
- ✅ Block popup & redirect iklan

### Navigasi Remote TV

| Tombol | Fungsi |
|--------|--------|
| D-pad | Pindah focus antar elemen (native WebView) |
| OK / Center | Klik elemen yang di-focus |
| Back | Kembali ke halaman sebelumnya |
| Menu | Buka overlay menu (bookmark, history, sites) |
| Info/Guide | Quick bookmark halaman saat ini |
| CH+ / CH- | Ganti situs streaming |
| Play/Pause | Play/pause video |

### Navigasi HP/Tablet

| Aksi | Fungsi |
|------|--------|
| Tap tombol ▶ (floating) | Buka overlay menu |
| Drag tombol ▶ | Pindahkan posisi button |
| Long press tombol ▶ | Quick bookmark |
| Touch/scroll | Navigasi normal |

### Build

1. Buka folder `StreamTV` di **Android Studio**
2. Tunggu Gradle sync
3. Run di device/emulator (Android TV atau HP)

### Minimum Requirements

- Android 5.0 (API 21)
- Target: Android 14 (API 34)
- Kotlin 1.9+

---

## Streaming Sites

| # | Site | URL |
|---|------|-----|
| 1 | iDlix | z1.idlixku.com |
| 2 | LK21 | tv10.lk21official.cc |
| 3 | Rebahin | rebahinxxi3.beauty |

> Sites can be added/changed in `SiteManager.kt`

---

## Project Structure

```
StreamTV/
├── app/src/main/
│   ├── kotlin/com/streamtv/app/
│   │   ├── MainActivity.kt           # Main activity + WebView
│   │   ├── data/
│   │   │   ├── BookmarkManager.kt    # Bookmark CRUD
│   │   │   ├── HistoryManager.kt     # History tracking
│   │   │   └── SiteManager.kt        # Multi-site config
│   │   └── ui/
│   │       └── OverlayMenu.kt        # Modern overlay menu
│   ├── res/
│   │   ├── drawable/                  # Icons & banner
│   │   ├── mipmap-*/                  # App icon (PIXELTV)
│   │   └── values/                    # Strings & themes
│   └── AndroidManifest.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## Changelog

### v1.5.1
- Fix menu can't scroll — wrapped in ScrollView
- Max menu height 85% of screen

### v1.5
- Floating button now draggable — move to any position
- Button position persists between sessions
- Menu overlay total redesign — modern glassmorphism style
- Scale + fade animation on menu open
- Menu items with icon, label, and arrow indicator
- Focus highlight for TV remote navigation in menu
- Modern confirm dialog for clear history
- Scrollable bookmark & history list
- Update Rebahin URL → rebahinxxi3.beauty

### v1.4.2
- Added "Build by Buildbox Studio" credit in overlay menu
- Added version info in menu
- Renamed menu title to "PIXELTV Menu"

### v1.4.1
- Version bump

### v1.4
- Removed virtual cursor (didn't work on TV)
- D-pad now passes through to WebView (native focus navigation)
- Inject tabindex to all clickable elements
- Yellow focus style for D-pad navigation
- Fixed "back to home" bug — removed domain whitelist, use blacklist only

### v1.3
- Added virtual cursor (experimental)
- Block auto-refresh for iDlix
- Block ad redirects with domain whitelist
- Extended ad domain list

### v1.2
- Renamed app to PIXELTV
- Updated icon
- Dark purple theme
- Updated site URLs (LK21, Rebahin)

### v1.0
- Initial release
- Fullscreen WebView + ad blocker
- Bookmark, History, Multi-site
- Overlay menu
- Android TV + Mobile support

---

## Notes

- Streaming site URLs may change anytime, update in `SiteManager.kt`
- For better ad blocking, combine with DNS AdGuard (Private DNS: `dns.adguard-dns.com`)
- For Play Store publishing, content must be replaced with legal sources

---

**Build by [Buildbox Studio](https://www.tiktok.com/@buildbox.studio)**



===============================================================================================================
# PIXELTV

Aplikasi Android TV + Mobile untuk streaming movie via WebView dengan fitur bookmark, history, dan multi-site.

**Version:** 1.5.1  
**Build by:** [Buildbox Studio](https://www.tiktok.com/@buildbox.studio)

## Fitur

- ✅ WebView fullscreen dengan ad blocker
- ✅ Kompatibel Android TV (D-pad native focus navigation)
- ✅ Kompatibel HP/Tablet (touch)
- ✅ Fullscreen video handler
- ✅ Custom User-Agent (desktop browser)
- ✅ Hardware acceleration
- ✅ Keep screen on saat streaming
- ✅ Immersive mode (hide status bar & nav bar)
- ✅ Bookmark — simpan halaman favorit
- ✅ History — riwayat tontonan otomatis
- ✅ Multi-site — 3 situs streaming, ganti pakai CH+/CH-
- ✅ Floating button draggable — geser ke mana aja, posisi tersimpan
- ✅ Modern overlay menu — glassmorphism design dengan animasi
- ✅ Media control — Play/Pause video via remote
- ✅ Anti auto-refresh (fix iDlix idle reload)
- ✅ Block popup & redirect iklan

## Navigasi Remote TV

| Tombol | Fungsi |
|--------|--------|
| D-pad | Pindah focus antar elemen (native WebView) |
| OK / Center | Klik elemen yang di-focus |
| Back | Kembali ke halaman sebelumnya |
| Menu | Buka overlay menu (bookmark, history, sites) |
| Info/Guide | Quick bookmark halaman saat ini |
| CH+ / CH- | Ganti situs streaming |
| Play/Pause | Play/pause video |

## Navigasi HP/Tablet

| Aksi | Fungsi |
|------|--------|
| Tap tombol ▶ (floating) | Buka overlay menu |
| Drag tombol ▶ | Pindahkan posisi button |
| Long press tombol ▶ | Quick bookmark |
| Touch/scroll | Navigasi normal |

## Situs Streaming Tersedia

| # | Situs | 
|---|-------|
| 1 | iDlix | 
| 2 | LK21 | 
| 3 | Rebahin | 

> Situs bisa ditambah/diubah di `SiteManager.kt`

## Build

1. Buka folder `StreamTV` di **Android Studio**
2. Tunggu Gradle sync
3. Run di device/emulator (Android TV atau HP)

## Minimum Requirements

- Android 5.0 (API 21)
- Target: Android 14 (API 34)
- Kotlin 1.9+

## Struktur Project

```
StreamTV/
├── app/src/main/
│   ├── kotlin/com/streamtv/app/
│   │   ├── MainActivity.kt           # Main activity + WebView
│   │   ├── data/
│   │   │   ├── BookmarkManager.kt    # Bookmark CRUD
│   │   │   ├── HistoryManager.kt     # History tracking
│   │   │   └── SiteManager.kt        # Multi-site config
│   │   └── ui/
│   │       └── OverlayMenu.kt        # Modern overlay menu
│   ├── res/
│   │   ├── drawable/                  # Icons & banner
│   │   ├── mipmap-*/                  # App icon (PIXELTV)
│   │   └── values/                    # Strings & themes
│   └── AndroidManifest.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Changelog

### v1.5.1
- Fix menu nggak bisa di-scroll — wrap dalam ScrollView
- Max height menu 85% layar, sisanya scrollable

### v1.5
- Floating button sekarang draggable — geser ke posisi mana aja
- Posisi button tersimpan (persist antar session)
- Menu overlay redesign total — modern glassmorphism style
- Animasi scale + fade saat menu muncul
- Menu items dengan icon, label, dan arrow indicator
- Focus highlight untuk navigasi TV remote di menu
- Confirm dialog hapus riwayat lebih modern
- Scrollable bookmark & history list
- Update URL Rebahin → rebahinxxi3.beauty

### v1.4.2
- Tambah kredit "Build by Buildbox Studio" di overlay menu
- Tambah info versi di menu
- Rename menu title ke "PIXELTV Menu"

### v1.4.1
- Version bump

### v1.4
- Hapus virtual cursor (nggak jalan di TV)
- D-pad sekarang pass-through ke WebView (native focus navigation)
- Inject tabindex ke semua elemen clickable
- Focus style kuning untuk navigasi D-pad
- Fix bug "balik ke home" — hapus domain whitelist, pakai blacklist saja

### v1.3
- Tambah virtual cursor (experimental)
- Block auto-refresh iDlix
- Block redirect iklan dengan domain whitelist
- Tambah ad domain list lebih lengkap

### v1.2
- Rename app ke PIXELTV
- Update icon
- Tema warna ungu gelap
- Update URL situs (LK21, Rebahin)

### v1.0
- Initial release
- WebView fullscreen + ad blocker
- Bookmark, History, Multi-site
- Overlay menu
- Android TV + Mobile support

## Catatan

- URL situs streaming bisa berubah sewaktu-waktu, update di `SiteManager.kt`
- Untuk ad blocking lebih optimal, kombinasikan dengan DNS AdGuard (Private DNS: `dns.adguard-dns.com`)
- Untuk publish ke Play Store, perlu ganti konten ke yang legal

---

**Build by [Buildbox Studio](https://www.tiktok.com/@buildbox.studio)**
