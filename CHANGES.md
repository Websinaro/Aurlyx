# Auralyx v2.1 — Full Premium Redesign + AD17 Converter

## What Changed (All Modified Files)

### app/build.gradle.kts
- `compileSdk`/`targetSdk` → 36 (Android 16)
- Compose BOM `2025.01.01`, Material3 stable
- Media3 `1.5.1`, Coil 3, core-splashscreen
- `material-icons-extended` for full icon set
- `isShrinkResources`, `isMinifyEnabled` for release

### AndroidManifest.xml
- Splash screen theme applied
- `android:enableOnBackInvokedCallback=true` (predictive back)
- `WRITE_EXTERNAL_STORAGE` for converter output (≤API29)
- `stopWithTask="false"` on service (ColorOS keep-alive)

---

## Brand New UI (Full Redesign)

### ui/theme/Color.kt
- Deep Violet900 background palette
- Vivid Indigo primary, Rose secondary, Cyan tertiary
- Full transparent-alpha token set (White06 → White100)
- Matching light-mode palette

### ui/theme/Theme.kt
- Complete Material 3 token mapping for both dark/light
- Optional dynamic color (Android 12+)

### ui/theme/Type.kt
- Premium type scale — Black/ExtraBold display, tighter letter-spacing

### ui/components/MediaCard.kt (full rewrite)
- `MediaCard` — shadow + spring-scale press, gradient scrim, Indigo play button
- `MediaListItem` — press-highlight background animation, AD17 inline badge
- `AD17Badge` — compact chip
- `AlbumArt` — Indigo↔Rose gradient placeholder
- `AD17ThumbnailImage` — cached bitmap loader
- `PlayingBars` — animated equalizer, configurable colour
- `ShimmerBox` — moving linear-gradient skeleton
- `PermissionScreen` — welcome full-screen with animated icon

### ui/components/GradientBackground.kt
- Subtle radial glow from background; no heavy gradient

### ui/components/SectionHeader.kt
- Larger title style, tighter padding

### ui/navigation/NavDestination.kt
- Added `Routes.CONVERTER`

### ui/navigation/AuralyxNavGraph.kt (full rewrite)
- Scale+fade default transitions (96 % → 100 %)
- Player: slide-up-from-half vertical transition
- Settings/Converter: slide-in-from-right
- Bottom nav with per-icon AnimatedContent swap
- NavigationBar tonal styling with Indigo indicator

### ui/home/HomeScreen.kt (full rewrite)
- Scroll-reactive translucent header
- `NowPlayingHeroCard` — blurred art background with live progress indicator
- `ShuffleBanner` — animated gradient gradient sweep
- Favorites, Recently Played, Most Played, Music Videos carousels
- Loading skeleton with ShimmerBox
- Pulse-animated empty state

### ui/player/MiniPlayerBar.kt (full rewrite)
- Rounded top sheet, gradient progress strip
- Indigo→Rose gradient play button with spring scale
- AnimatedContent play/pause icon swap

### ui/player/PlayerScreen.kt (full rewrite)
- Audio layout: blurred art backdrop, scroll-collapsible
- Album art spring-scale with isPlaying, glow layer behind art card
- Scroll-reactive artwork parallax alpha
- Custom gradient Slider (no track override needed)
- Sleep timer dialog with 5 presets + cancel
- Favourite / Sleep / Video toggle in top bar
- Animated queue reveal with up-next count
- Video layout: glass icon buttons, double-tap seek ±10s, fullscreen toggle

### ui/library/LibraryScreen.kt (full rewrite)
- `displaySmall` Black header
- Animated tab content with direction-aware slide
- Album grid with song-count chip, tap-ready
- Artists with circle art avatars
- Folders with Indigo icon boxes

### ui/search/SearchScreen.kt (full rewrite)
- Filled TextField with rounded pill shape, no underline
- AnimatedContent for blank/empty/results states
- Pulsing search icon empty state

### ui/settings/SettingsScreen.kt (full rewrite)
- Sectioned list with icon boxes
- "Convert Video to aD17" action row → ConverterScreen
- Dynamic Color toggle (API 31+)
- Version / format info rows

---

## New: Video to AD17 Converter

### converter/AD17Converter.kt (NEW)
- Full MediaCodec pipeline: video H.264 + audio AAC re-encode
- `DefaultLoadControl` tuned buffer for gapless smoothness
- Adjustable quality: Standard (720p/192k), High (1080p/256k), Ultra (1440p/320k)
- XOR-obfuscation with shared `AD17_KEY` (symmetric with ThumbnailUtils)
- Coroutine-based, cancellable, progress callbacks

### ui/converter/ConverterViewModel.kt (NEW)
- `ConversionJob` queue with QUEUED/CONVERTING/DONE/FAILED/CANCELLED states
- Background-safe via `viewModelScope`
- Retry-failed, cancel, remove queue operations

### ui/converter/ConverterScreen.kt (NEW)
- Quality preset picker (icon + bitrate label)
- System file picker (`video/*`)
- Per-job cards with animated spinning progress, status icons, linear bar
- Empty state with pulsing icon
- Info banner explaining the format

### di/ConverterModule.kt (NEW)
- Hilt singleton provision of AD17Converter

---

## Audio Engine Improvements

### di/PlayerModule.kt
- `DefaultLoadControl` — minBuffer 15s, maxBuffer 60s, fast 1.5s play start
- `setHandleAudioBecomingNoisy(true)` — auto-pause on headphone unplug
- `skipSilenceEnabled = false` — preserve full dynamic range
- Audio focus handling enabled

### utils/ThumbnailUtils.kt
- XOR key shared with `AD17Converter.AD17_KEY` (symmetric encode/decode)
- Decodes .aD17 → temp .mp4 before frame extraction or playback

---

## Build Instructions
```bash
# Debug
./gradlew assembleDebug

# Release (needs keystore configured in build.gradle.kts)
./gradlew assembleRelease
```

Min SDK: 24 | Target SDK: 36 | Kotlin: 2.0.21 | Compose BOM: 2025.01.01
