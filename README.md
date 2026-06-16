# Studio — Android App
### Download · Edit · Create
**By Rahul Sah | Birgunj, Nepal**

---

## Overview

Studio is a professional-grade Android application built in **Kotlin + Jetpack Compose** that combines:
- **Universal Downloader** — download video, audio, and images from YouTube, Instagram, TikTok, Twitter/X, Facebook, Vimeo, Reddit, SoundCloud, and any website
- **Built-in Browser** — browse any site and detect downloadable media automatically
- **Pro Video Editor** — CapCut-style editor powered by FFmpeg with filters, trimming, color grading, text overlays, speed control, and 4K export
- **Media Library** — organized gallery of all downloaded and edited media

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| State | ViewModel + StateFlow |
| Networking | OkHttp 4 + Retrofit + Jsoup |
| Media Playback | ExoPlayer (Media3) |
| Media Editing | mobile-ffmpeg-full |
| Background Work | WorkManager |
| Image Loading | Coil 2 |
| Build | Gradle 8 + AGP 8.1 |
| Min SDK | Android 7 (API 24) |
| Target SDK | Android 14 (API 34) |

---

## Project Structure

```
StudioApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/rahulsah/studio/
│   │   │   ├── MainActivity.kt                  # Entry point, NavHost
│   │   │   ├── data/
│   │   │   │   ├── model/Models.kt              # All data models
│   │   │   │   ├── repository/DownloadRepository.kt  # URL analysis + downloads
│   │   │   │   ├── service/DownloadService.kt   # Foreground download service
│   │   │   │   └── worker/DownloadWorker.kt     # WorkManager download task
│   │   │   ├── ui/
│   │   │   │   ├── theme/                       # Dark violet theme, typography
│   │   │   │   ├── components/Components.kt     # Reusable UI components
│   │   │   │   ├── Navigation.kt                # Screen routes
│   │   │   │   └── screens/
│   │   │   │       ├── HomeScreen.kt            # Dashboard
│   │   │   │       ├── DownloaderScreen.kt      # URL downloader + queue
│   │   │   │       ├── EditorScreen.kt          # Full CapCut-style editor
│   │   │   │       ├── LibraryAndBrowserScreens.kt  # Media library + WebView browser
│   │   │   │       └── SettingsScreen.kt        # App settings
│   │   │   ├── viewmodel/
│   │   │   │   ├── DownloadViewModel.kt
│   │   │   │   ├── EditorViewModel.kt           # Full editor state + FFmpeg export
│   │   │   │   └── LibraryViewModel.kt
│   │   │   └── utils/
│   │   │       ├── MediaStoreHelper.kt          # Scan local media
│   │   │       └── PermissionHelper.kt
│   │   ├── AndroidManifest.xml
│   │   └── res/
│   │       ├── values/  (strings, colors, themes)
│   │       ├── drawable/ (splash icon)
│   │       └── xml/ (file_paths, backup rules)
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## Supported Platforms (Downloader)

| Platform | Video | Audio | Notes |
|---|---|---|---|
| YouTube | ✅ | ✅ | oEmbed API for metadata |
| Instagram | ✅ | — | Reels, Posts, TV |
| TikTok | ✅ | — | oEmbed API |
| Twitter / X | ✅ | — | OG meta parsing |
| Facebook | ✅ | — | Video posts |
| Vimeo | ✅ | — | |
| Reddit | ✅ | — | |
| SoundCloud | — | ✅ | |
| Any site | ✅ | ✅ | OG tags + direct links |

> **Production note:** For full YouTube/TikTok stream extraction, integrate [yt-dlp](https://github.com/yt-dlp/yt-dlp) as a native binary or via a self-hosted companion API.

---

## Editor Features

| Feature | Details |
|---|---|
| Trim & Cut | Dual-handle timeline, frame-level precision |
| Filters | 10 built-in (Vivid, B&W, Warm, Cool, Cinematic, Vintage, Neon, Drama…) |
| Color Adjust | Brightness, Contrast, Saturation, Exposure, Sharpness, Vignette |
| Speed | 0.1× to 4× |
| Reverse | Reverse video playback |
| Text Overlays | Position, font size, color, duration |
| Audio | Volume, Mute, per-clip control |
| Aspect Ratio | Free, 16:9, 9:16, 1:1, 4:3, 21:9 and more |
| Export | MP4/WebM, Low/Medium/High/Ultra quality via FFmpeg |

---

## Setup Instructions

### 1. Clone / Open in Android Studio
```bash
# Open Android Studio → File → Open → select StudioApp/
```

### 2. Add wrapper files
Generate Gradle wrapper:
```bash
cd StudioApp
gradle wrapper --gradle-version=8.3
```
Or copy `gradle-wrapper.jar` and `gradle-wrapper.properties` from any existing Android project.

### 3. Sync & Build
```
Build → Make Project (Ctrl+F9)
```

### 4. Run on device / emulator
Minimum: Android 7.0 (API 24). Recommended: Android 11+ for MediaStore scoped storage.

---

## Permissions Required

| Permission | Why |
|---|---|
| INTERNET | Download media, browse sites |
| READ_MEDIA_VIDEO/IMAGES/AUDIO | Access device media |
| WRITE_EXTERNAL_STORAGE (≤API29) | Save downloads |
| FOREGROUND_SERVICE | Background download progress |
| POST_NOTIFICATIONS | Download progress notifications |

---

## Production Enhancements (Roadmap)

- [ ] Integrate yt-dlp binary via JNI for full stream extraction
- [ ] Room DB for persistent download history
- [ ] ExoPlayer AndroidView in EditorScreen for real video preview
- [ ] FFmpeg executeAsync with real progress callbacks
- [ ] Multi-clip timeline (like CapCut)
- [ ] Stickers / emoji overlay library
- [ ] Background music track selection
- [ ] Cloud backup via Google Drive API
- [ ] Batch download queue

---

## Creator

**Rahul Sah**  
Freelance Full-Stack & Mobile Developer  
Birgunj, Nepal  
Stack: Kotlin · Flutter · Node.js · PostgreSQL · React · Python
