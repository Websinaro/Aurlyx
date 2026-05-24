# Auralyx Optimization & Feature Summary

## Build & Dependencies
- `compileSdk` / `targetSdk` bumped to **36** (Android 16)
- Kotlin `2.0.21`, AGP `8.7.3`, KSP `2.0.21-1.0.28`
- Compose BOM `2025.01.01` (Material 3 stable)
- Media3 `1.5.1` (latest stable)
- Coil 3 (`coil3-compose`, `coil3-network-okhttp`) — faster image pipeline
- `androidx.core:core-splashscreen` for faster cold-start
- `isShrinkResources = true` in release builds
- `gradle.properties`: parallel builds, incremental Kotlin

## Android 16 / Edge-to-Edge
- `enableEdgeToEdge()` replaces deprecated `WindowCompat.setDecorFitsSystemWindows`
- `android:enableOnBackInvokedCallback="true"` for predictive back gesture
- Removed stale `INTERNET` permission (not needed for local playback)

## New Features
| Feature | Where |
|---------|-------|
| **Favorites** | Heart icon in player toolbar; stored in Room |
| **Most Played** | Home screen carousel (sorted by `play_count`) |
| **Play count** | Auto-incremented on every `updateLastPlayed` call |
| **Sleep Timer** | Moon icon in player toolbar → dialog (15/30/45/60/90 min) |
| **Dynamic Color** | Settings toggle (Android 12+ only) |
| **Improved search** | Relevance-sorted (prefix match first, then play_count) |

## Database (v1 → v2)
- `MediaEntity` gains: `play_count INT`, `is_favorite BOOLEAN`
- New indices: `is_favorite`, `play_count`
- `fallbackToDestructiveMigration()` handles the schema bump safely
- `updateLastPlayed` now also increments `play_count` atomically

## Performance
- Column indices cached before cursor loop in `scanAudio()` — faster scan
- Minimum track duration raised 5s → 10s (avoids ringtone noise)
- LazyColumn `key = { it.id }` preserved everywhere for stable recomposition
- `derivedStateOf` for art alpha scroll calculation (avoids recomposition storm)

## ColorOS / Background Playback
- `START_STICKY` on `onStartCommand`
- `stopWithTask="false"` in manifest
- Notification channel importance stays `IMPORTANCE_LOW`

## Code Quality
- All one-liner minified files reformatted to readable style
- Dead `INTERNET` permission removed
- `PreferencesManager` gains `dynamicColor` key
- `PlayerState` gains `sleepTimerEndsAt` / `sleepTimerActive`

## Build Instructions
```
./gradlew assembleDebug
./gradlew assembleRelease   # needs keystore config
```
Minimum SDK: 24   Target SDK: 36
