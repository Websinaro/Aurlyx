# Auralyx Player 🎵

A production-ready Android music + video player built with **Kotlin**, **Jetpack Compose**, **ExoPlayer (Media3)**, **Hilt**, and **Room**.

---

## Architecture

```
auralyx/
├── app/src/main/java/com/auralyx/
│   ├── AuralyxApp.kt               ← Hilt application entry point
│   ├── MainActivity.kt             ← Single activity host
│   │
│   ├── data/
│   │   ├── local/
│   │   │   ├── dao/MediaDao.kt     ← Room DAO (all SQL queries)
│   │   │   ├── entity/MediaEntity.kt
│   │   │   └── database/AuralyxDatabase.kt
│   │   └── repository/MediaRepositoryImpl.kt ← MediaStore + .aD17 scanner
│   │
│   ├── domain/
│   │   ├── model/                  ← MediaItem, Album, Artist, Folder, PlayerState
│   │   ├── repository/MediaRepository.kt (interface)
│   │   └── usecase/                ← GetAllSongs, GetMusicVideos, Search, Scan
│   │
│   ├── di/                         ← Hilt modules (DB, Repo, Player, DataStore)
│   │
│   ├── player/AuralyxPlayer.kt     ← ExoPlayer wrapper, queue, audio/video toggle
│   │
│   ├── service/AuralyxPlaybackService.kt ← MediaSessionService (background play)
│   │
│   ├── ui/
│   │   ├── home/                   ← HomeScreen + HomeViewModel
│   │   ├── library/                ← LibraryScreen (Songs/Albums/Artists/Folders/Videos tabs)
│   │   ├── player/                 ← PlayerScreen, PlayerViewModel, MiniPlayerBar
│   │   ├── search/                 ← SearchScreen with real-time debounce
│   │   ├── settings/               ← SettingsScreen (theme, video mode, scan)
│   │   ├── components/             ← MediaCard, MediaListItem, AlbumArt, PlayingIndicator
│   │   ├── navigation/             ← NavGraph + NavDestinations
│   │   └── theme/                  ← Material3 colors, typography
│   │
│   └── utils/
│       ├── PermissionUtils.kt      ← Runtime permission helpers (API 24–34)
│       ├── PreferencesManager.kt   ← DataStore preferences
│       └── ThumbnailUtils.kt       ← Video frame extraction + .aD17 temp copy
```

---

## .aD17 File Handling

`.aD17` files are MP4 videos with a custom extension. Auralyx:

1. **Detects** them by extension during storage scan (`MediaRepositoryImpl.scanDir`)
2. **Copies** them to a temp `.mp4` in cache at play-time (`ThumbnailUtils.resolveAD17Path`)
3. **Plays** via ExoPlayer — default is **audio-only** (video renderer disabled via `DefaultTrackSelector`)
4. **Toggles** video on/off from the Player screen top bar (the Videocam icon)
5. Shows a purple `.aD17` badge on list items and player screen

---

## Key Features

| Feature | Implementation |
|---|---|
| Background play | `AuralyxPlaybackService` extends `MediaSessionService` |
| Notification controls | Media3 handles this automatically via `MediaSession` |
| Mini player | `MiniPlayerBar` above bottom nav, shown while music plays |
| Album art blur BG | `AsyncImage` + `Modifier.blur(80.dp)` on player screen |
| Scroll-fade art | `derivedStateOf` on `LazyListState` → `animateFloatAsState` |
| Real-time search | `Flow.debounce(300ms)` + `flatMapLatest` |
| Shuffle | ExoPlayer native `shuffleModeEnabled` |
| Repeat | ExoPlayer native `repeatMode` (OFF → ALL → ONE) |
| Dark/light theme | DataStore preference → `AuralyxTheme(darkTheme)` |
| Video thumbnail | `MediaMetadataRetriever.getFrameAtTime` |

---

## Setup & Build

### Requirements
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 35

### Steps
1. Open this folder in Android Studio
2. Let Gradle sync
3. Run on a device or emulator (API 24+)
4. Grant storage permission on first launch
5. Tap **Scan Storage** (or let auto-scan run) to index media

### First Run
On first install the database is empty. The app will:
- Request `READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE` permission
- Auto-scan if "Scan on Launch" is enabled (default: on)
- Populate the Home and Library screens

---

## Dependencies

| Library | Version | Purpose |
|---|---|---|
| Compose BOM | 2024.09.00 | UI |
| Media3 ExoPlayer | 1.4.1 | Playback |
| Media3 Session | 1.4.1 | Background + notification |
| Hilt | 2.51.1 | Dependency injection |
| Room | 2.6.1 | Local database |
| Coil | 2.7.0 | Image loading |
| DataStore | 1.1.1 | Preferences |
| WorkManager | 2.9.1 | Background tasks |

---

## Permissions

| Permission | Required for |
|---|---|
| `READ_MEDIA_AUDIO` (API 33+) | Scan audio files |
| `READ_MEDIA_VIDEO` (API 33+) | Scan .aD17 / video files |
| `READ_EXTERNAL_STORAGE` (API <33) | All file access |
| `FOREGROUND_SERVICE` | Background playback service |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | API 34+ foreground type |
| `POST_NOTIFICATIONS` (API 33+) | Playback notification |

---

## ProGuard / R8

ProGuard rules are pre-configured in `app/proguard-rules.pro` for ExoPlayer, Hilt, Room, and Coil. Release builds are configured with `isMinifyEnabled = true`.

---

## Customisation

- **Accent colour**: Change `PurpleAccent` in `ui/theme/Color.kt`
- **aD17 temp path**: Modify `ThumbnailUtils.resolveAD17Path`
- **Scan filters**: Add extensions to `MediaRepositoryImpl.scanDir`
- **Default video mode**: Toggle in Settings or set default in `PreferencesManager`
