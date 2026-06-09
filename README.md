# PIXELTV

PIXELTV - Android TV & Mobile streaming app with built-in ad blocker, multi-site support, bookmark, history, and D-pad navigation. Features draggable floating menu, modern glassmorphism UI, and fullscreen video handler. Built with Kotlin + WebView.

**Version:** 2.2.0  
**Build by:** [Buildbox Studio](https://www.tiktok.com/@buildbox.studio)

<img width="1158" height="540" alt="image" src="https://github.com/user-attachments/assets/682addf6-7e01-4770-9f57-6f31cc47c4f3" />

<img width="1158" height="540" alt="image" src="https://github.com/user-attachments/assets/f6d5611c-0042-4771-b179-a887e41b2aa9" />

<img width="1158" height="540" alt="image" src="https://github.com/user-attachments/assets/92efa5c5-a0cf-4486-9980-e7df95858097" />

<img width="1158" height="540" alt="image" src="https://github.com/user-attachments/assets/9bb8d971-8080-4550-97bc-2216fecd63e8" />



---

## 🇬🇧 English

### Features

- ✅ **Splash Screen** — animated logo on launch
- ✅ **Home Screen** — pick a site or continue watching
- ✅ **Search** — search movies directly from the app
- ✅ **Gesture Navigation** — swipe left (switch site), swipe right (go back), swipe down (refresh)
- ✅ **Confirm Exit** — no more accidental exits
- ✅ **Snackbar Notifications** — error messages with retry button
- ✅ Fullscreen WebView with ad blocker
- ✅ Compatible with Android TV (D-pad native focus navigation)
- ✅ Compatible with Phone/Tablet (touch + gestures)
- ✅ Fullscreen video handler
- ✅ Custom User-Agent (desktop browser)
- ✅ Hardware acceleration
- ✅ Keep screen on while streaming
- ✅ Immersive mode (hide status bar & nav bar)
- ✅ Bookmark — save favorite pages
- ✅ History — automatic watch history
- ✅ Multi-site — 3 streaming sites, switch with CH+/CH- or swipe
- ✅ Draggable floating button — move anywhere, position saved
- ✅ Modern overlay menu — glassmorphism design with animation
- ✅ Media control — Play/Pause video via remote
- ✅ Anti auto-refresh (fix idle reload)
- ✅ Block popup & ad redirects

### TV Remote Navigation

| Button | Function |
|--------|----------|
| D-pad | Move focus between elements |
| OK / Center | Click focused element |
| Back | Go back / Confirm exit |
| Menu | Open overlay menu |
| Info/Guide | Quick bookmark |
| CH+ / CH- | Switch streaming site |
| Play/Pause | Play/pause video |
| Search | Open search dialog |

### Phone/Tablet Gestures

| Gesture | Function |
|---------|----------|
| Swipe Left | Switch to next site |
| Swipe Right | Go back |
| Swipe Down | Refresh page |
| Tap ▶ button | Open menu |
| Drag ▶ button | Move button |
| Long press ▶ | Quick bookmark |

### Minimum Requirements

- Android 5.0 (API 21)
- Target: Android 14 (API 34)
- Kotlin 1.9+

---

## 🇮🇩 Bahasa Indonesia

### Fitur

- ✅ **Splash Screen** — logo animasi saat buka app
- ✅ **Home Screen** — pilih situs atau lanjut nonton
- ✅ **Search** — cari film langsung dari app
- ✅ **Gesture Navigation** — swipe kiri (ganti situs), swipe kanan (kembali), swipe bawah (refresh)
- ✅ **Confirm Exit** — nggak lagi keluar nggak sengaja
- ✅ **Snackbar Notifications** — pesan error dengan tombol retry
- ✅ WebView fullscreen dengan ad blocker
- ✅ Kompatibel Android TV (navigasi D-pad native)
- ✅ Kompatibel HP/Tablet (touch + gesture)
- ✅ Fullscreen video handler
- ✅ Custom User-Agent (desktop browser)
- ✅ Hardware acceleration
- ✅ Layar tetap nyala saat streaming
- ✅ Immersive mode
- ✅ Bookmark — simpan halaman favorit
- ✅ History — riwayat tontonan otomatis
- ✅ Multi-site — 3 situs streaming
- ✅ Floating button draggable
- ✅ Menu overlay modern — glassmorphism
- ✅ Media control — Play/Pause via remote
- ✅ Anti auto-refresh
- ✅ Block popup & redirect iklan

### Navigasi Remote TV

| Tombol | Fungsi |
|--------|--------|
| D-pad | Pindah focus antar elemen |
| OK / Center | Klik elemen yang di-focus |
| Back | Kembali / Confirm keluar |
| Menu | Buka overlay menu |
| Info/Guide | Quick bookmark |
| CH+ / CH- | Ganti situs streaming |
| Play/Pause | Play/pause video |
| Search | Buka dialog pencarian |

### Gesture HP/Tablet

| Gesture | Fungsi |
|---------|--------|
| Swipe Kiri | Ganti ke situs berikutnya |
| Swipe Kanan | Kembali |
| Swipe Bawah | Refresh halaman |
| Tap tombol ▶ | Buka menu |
| Drag tombol ▶ | Pindahkan button |
| Long press ▶ | Quick bookmark |

---

## Streaming Sites

| # | Site | URL |
|---|------|-----|
| 1 | iDlix | z1.idlixku.com |
| 2 | LK21 | tv10.lk21official.cc |
| 3 | Rebahin | rebahinxxi3.beauty |
| 4 | Nobargasi | www.nobargasi.my.id |

> Sites can be added/changed in `SiteManager.kt`

---

## Project Structure

```
StreamTV/
├── app/src/main/
│   ├── kotlin/com/streamtv/app/
│   │   ├── SplashActivity.kt         # Splash screen
│   │   ├── HomeActivity.kt           # Home / site picker
│   │   ├── MainActivity.kt           # WebView player
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

### v2.2.0
- NEW: **Red / Black / Grey theme** — complete UI redesign across all screens (splash, home, menu, player)
- NEW: Added **Nobargasi** streaming site
- FIX: Swipe gestures too sensitive on phone — drastically increased thresholds
  - Swipe distance: 100px → 250px
  - Swipe velocity: 100 → 1200
  - Direction must be clearly dominant (2.5x ratio)
  - Swipe-down refresh only triggers from top 25% of screen (pull-to-refresh style)
  - TV remote navigation unaffected
- Added centralized `AppTheme.kt` color palette

### v2.1.0
- NEW: **JS Navigation Layer** for Android TV remote
  - Scans all clickable elements on page
  - D-pad moves between elements based on spatial position (nearest in direction)
  - Yellow highlight border shows which element is selected
  - OK/Enter clicks the highlighted element
  - Auto-scroll when navigating to off-screen elements
  - Auto-rescan on page changes (MutationObserver)
  - Falls back to page scroll if no element found in direction
- IMPROVED: TV remote now actually usable for browsing & selecting movies
- IMPROVED: Navigation feels like a native TV app

### v2.0
- NEW: Splash screen with animated logo
- NEW: Home screen — site picker + continue watching
- NEW: Search dialog — search movies on current site
- NEW: Gesture navigation — swipe left/right/down
- NEW: Confirm exit dialog (Back when can't go back)
- NEW: Snackbar with retry button on error
- NEW: Home button in exit dialog to go back to site picker
- IMPROVED: App flow: Splash → Home → Player
- IMPROVED: 3-activity architecture for better UX

### v1.5.1
- Fix menu can't scroll — wrapped in ScrollView

### v1.5
- Floating button now draggable
- Menu overlay redesign — glassmorphism style
- Update Rebahin URL

### v1.4.2
- Added credit & version in menu

### v1.4
- Removed virtual cursor
- D-pad pass-through to WebView
- Fixed "back to home" bug

### v1.3
- Block auto-refresh
- Block ad redirects

### v1.2
- Renamed to PIXELTV
- Updated icon & theme

### v1.0
- Initial release

---

**Build by [Buildbox Studio](https://www.tiktok.com/@buildbox.studio)**
