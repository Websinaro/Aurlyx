package com.auralyx.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.auralyx.domain.model.MediaItem
import com.auralyx.domain.model.PlayerState
import com.auralyx.domain.model.RepeatMode
import com.auralyx.ui.components.*
import com.auralyx.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(onBack: () -> Unit, vm: PlayerViewModel = hiltViewModel()) {
    val state   by vm.state.collectAsState()
    val activity = LocalContext.current as? Activity

    DisposableEffect(state.isVideoEnabled && state.isPlaying) {
        if (state.isVideoEnabled && state.isPlaying)
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    LaunchedEffect(state.currentItem?.id) {
        state.currentItem?.id?.let { vm.updateLastPlayed(it) }
    }

    if (state.isVideoEnabled && state.currentItem?.isAD17 == true) {
        VideoPlayerLayout(state, vm, activity, onBack)
    } else {
        AudioPlayerLayout(state, vm, onBack)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  VIDEO LAYOUT
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun VideoPlayerLayout(state: PlayerState, vm: PlayerViewModel, activity: Activity?, onBack: () -> Unit) {
    var showControls by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }

    LaunchedEffect(showControls, state.isPlaying) {
        if (showControls && state.isPlaying) { delay(3500); showControls = false }
    }
    DisposableEffect(isFullscreen) {
        activity?.requestedOrientation = if (isFullscreen)
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }

    Box(
        Modifier.fillMaxSize().background(Color.Black)
            .pointerInput(Unit) { detectTapGestures { showControls = !showControls } }
    ) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply {
                player = vm.player.exoPlayer; useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setBackgroundColor(android.graphics.Color.BLACK)
            }},
            update  = { it.player = vm.player.exoPlayer },
            modifier = Modifier.fillMaxSize()
        )
        AnimatedVisibility(showControls, enter = fadeIn(tween(200)), exit = fadeOut(tween(200))) {
            VideoOverlay(state, vm, isFullscreen, onBack) { isFullscreen = !isFullscreen }
        }
    }
}

@Composable
private fun VideoOverlay(
    state: PlayerState, vm: PlayerViewModel,
    isFullscreen: Boolean, onBack: () -> Unit, onToggleFs: () -> Unit
) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                0.00f to Color.Black.copy(0.72f),
                0.28f to Color.Transparent,
                0.72f to Color.Transparent,
                1.00f to Color.Black.copy(0.88f)
            )
        )
    ) {
        // Top bar
        Row(
            Modifier.fillMaxWidth().align(Alignment.TopCenter)
                .statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onBack) { Icon(Icons.Rounded.KeyboardArrowDown, null, tint = Color.White) }
            Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                Text(state.currentItem?.title ?: "", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(state.currentItem?.displayArtist ?: "", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.75f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton({ vm.toggleVideo() }) { Icon(Icons.Rounded.AudioFile, null, tint = Color.White) }
            IconButton(onToggleFs) { Icon(if (isFullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen, null, tint = Color.White) }
        }

        // Centre transport
        Row(Modifier.align(Alignment.Center), Arrangement.spacedBy(16.dp), Alignment.CenterVertically) {
            GlassIconBtn(Icons.Rounded.Replay10, 48.dp, 28.dp) { vm.seekTo((state.progress - 10_000).coerceAtLeast(0)) }
            GlassIconBtn(Icons.Rounded.SkipPrevious, 54.dp, 32.dp) { vm.skipToPrev() }
            BigPlayBtn(state.isPlaying, state.isBuffering) { vm.togglePlayPause() }
            GlassIconBtn(Icons.Rounded.SkipNext, 54.dp, 32.dp) { vm.skipToNext() }
            GlassIconBtn(Icons.Rounded.Forward10, 48.dp, 28.dp) { vm.seekTo((state.progress + 10_000).coerceAtMost(state.duration)) }
        }

        // Bottom controls
        Column(
            Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                .navigationBarsPadding().padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(fmtMs(state.progress), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.85f))
                Text(state.currentItem?.durationFormatted ?: "--:--", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.85f))
            }
            Slider(
                value           = state.progressFraction,
                onValueChange   = { vm.seekToFraction(it) },
                modifier        = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor          = Color.White,
                    activeTrackColor    = Indigo,
                    inactiveTrackColor  = Color.White.copy(0.3f)
                )
            )
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                IconButton({ vm.toggleShuffle() }) {
                    Icon(Icons.Rounded.Shuffle, null, tint = if (state.shuffleEnabled) Indigo else Color.White.copy(0.65f), modifier = Modifier.size(20.dp))
                }
                IconButton({ vm.cycleRepeatMode() }) {
                    Icon(if (state.repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat, null,
                        tint = if (state.repeatMode != RepeatMode.OFF) Indigo else Color.White.copy(0.65f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  AUDIO LAYOUT
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun AudioPlayerLayout(state: PlayerState, vm: PlayerViewModel, onBack: () -> Unit) {
    val queueListState = rememberLazyListState()
    var showSleepDialog by remember { mutableStateOf(false) }
    var showQueue       by remember { mutableStateOf(false) }

    if (showSleepDialog) SleepTimerDialog(
        active   = state.sleepTimerActive,
        onDismiss = { showSleepDialog = false },
        onSet     = { vm.setSleepTimer(it); showSleepDialog = false },
        onCancel  = { vm.cancelSleepTimer(); showSleepDialog = false }
    )

    Box(Modifier.fillMaxSize()) {
        // ── Immersive blurred art background ─────────────────────────────
        val artUri = state.currentItem?.albumArtUri
        if (state.currentItem?.isAD17 == true) {
            AD17ThumbnailImage(state.currentItem!!.path, Modifier.fillMaxSize().blur(80.dp).alpha(0.25f))
        } else if (!artUri.isNullOrBlank()) {
            AsyncImage(artUri, null, Modifier.fillMaxSize().blur(80.dp).alpha(0.25f), contentScale = ContentScale.Crop)
        }
        // Dark gradient overlay
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                0.0f to MaterialTheme.colorScheme.background.copy(0.82f),
                0.4f to MaterialTheme.colorScheme.background.copy(0.92f),
                1.0f to MaterialTheme.colorScheme.background.copy(0.98f)
            )
        ))

        // ── Main content ──────────────────────────────────────────────────
        LazyColumn(
            state           = queueListState,
            modifier        = Modifier.fillMaxSize(),
            contentPadding  = PaddingValues(bottom = 32.dp)
        ) {
            // Top bar
            item {
                PlayerTopBar(
                    state           = state,
                    onBack          = onBack,
                    onFavorite      = { vm.toggleFavorite() },
                    onSleepTimer    = { showSleepDialog = true },
                    onToggleVideo   = { vm.toggleVideo() }
                )
            }

            // Album art
            item {
                PlayerArtwork(state = state, modifier = Modifier.animateItem())
            }

            // Song info
            item {
                PlayerSongInfo(state = state, onFavorite = { vm.toggleFavorite() })
            }

            // Seekbar
            item {
                PlayerSeekBar(state = state, onSeek = { vm.seekToFraction(it) })
            }

            // Transport controls
            item {
                PlayerControls(
                    state      = state,
                    onPlayPause = { vm.togglePlayPause() },
                    onPrev     = { vm.skipToPrev() },
                    onNext     = { vm.skipToNext() },
                    onShuffle  = { vm.toggleShuffle() },
                    onRepeat   = { vm.cycleRepeatMode() }
                )
            }

            // Queue toggle
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(0.6f))
                        .clickable { showQueue = !showQueue }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Row(Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
                        Icon(Icons.Rounded.QueueMusic, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text("Queue", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Row(Arrangement.spacedBy(6.dp), Alignment.CenterVertically) {
                        Text("${(state.queue.size - state.queueIndex - 1).coerceAtLeast(0)} upcoming", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Icon(
                            if (showQueue) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Queue items
            if (showQueue) {
                val upNext = state.queue.drop(state.queueIndex + 1)
                if (upNext.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                            Text("Queue is empty", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    itemsIndexed(upNext, key = { _, it -> "q_${it.id}" }) { idx, item ->
                        QueueRow(item, idx + 1, Modifier.animateItem())
                    }
                }
            }
        }
    }
}

// ── Top Bar ──────────────────────────────────────────────────────────────────
@Composable
private fun PlayerTopBar(
    state: PlayerState,
    onBack: () -> Unit,
    onFavorite: () -> Unit,
    onSleepTimer: () -> Unit,
    onToggleVideo: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 6.dp, vertical = 6.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        IconButton(onBack) {
            Icon(Icons.Rounded.KeyboardArrowDown, "Back", Modifier.size(32.dp))
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NOW PLAYING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
            if (state.currentItem?.isAD17 == true) AD17Badge(Modifier.padding(top = 2.dp))
        }
        Row {
            val isFav = state.currentItem?.isFavorite == true
            IconButton(onFavorite) {
                Icon(
                    if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    "Fav",
                    tint = if (isFav) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onSleepTimer) {
                Icon(
                    Icons.Outlined.Bedtime, "Sleep",
                    tint = if (state.sleepTimerActive) Indigo else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.currentItem?.isAD17 == true) {
                IconButton(onToggleVideo) {
                    Icon(Icons.Rounded.Videocam, "Video", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// ── Artwork ──────────────────────────────────────────────────────────────────
@Composable
private fun PlayerArtwork(state: PlayerState, modifier: Modifier = Modifier) {
    val artScale by animateFloatAsState(
        if (state.isPlaying) 1f else 0.88f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "artScale"
    )

    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 8.dp)
            .aspectRatio(1f)
    ) {
        // Glow behind art
        Box(
            Modifier.fillMaxSize().scale(1.05f).blur(48.dp)
                .background(IndigoGlow.copy(if (state.isPlaying) 0.5f else 0.25f), RoundedCornerShape(32.dp))
        )
        // Art card
        Box(
            Modifier.fillMaxSize().scale(artScale)
                .shadow(if (state.isPlaying) 40.dp else 12.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
        ) {
            when {
                state.currentItem?.isAD17 == true ->
                    AD17ThumbnailImage(state.currentItem!!.path, Modifier.fillMaxSize())
                else ->
                    AlbumArt(state.currentItem?.albumArtUri, false, Modifier.fillMaxSize())
            }
        }
    }
}

// ── Song Info ─────────────────────────────────────────────────────────────────
@Composable
private fun PlayerSongInfo(state: PlayerState, onFavorite: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 6.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                state.currentItem?.title ?: "Nothing playing",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                state.currentItem?.displayArtist ?: "—",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!state.currentItem?.displayAlbum.isNullOrBlank()) {
                Text(
                    state.currentItem!!.displayAlbum,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── Seekbar ───────────────────────────────────────────────────────────────────
@Composable
private fun PlayerSeekBar(state: PlayerState, onSeek: (Float) -> Unit) {
    Column(Modifier.padding(horizontal = 24.dp)) {
        // Custom gradient seek track
        var dragging by remember { mutableStateOf(false) }
        val animProg by animateFloatAsState(
            if (dragging) state.progressFraction else state.progressFraction,
            if (dragging) tween(0) else tween(500),
            label = "seek"
        )
        Slider(
            value         = state.progressFraction,
            onValueChange = { onSeek(it) },
            onValueChangeFinished = { dragging = false },
            modifier      = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor         = MaterialTheme.colorScheme.primary,
                activeTrackColor   = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(0.25f)
            )
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), Arrangement.SpaceBetween) {
            Text(fmtMs(state.progress), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (state.isBuffering) {
                Text("Buffering…", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Text(state.currentItem?.durationFormatted ?: "--:--", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Transport Controls ────────────────────────────────────────────────────────
@Composable
private fun PlayerControls(
    state: PlayerState,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        Arrangement.SpaceEvenly, Alignment.CenterVertically
    ) {
        // Shuffle
        IconButton(onShuffle, Modifier.size(50.dp)) {
            Icon(Icons.Rounded.Shuffle, null, Modifier.size(22.dp),
                tint = if (state.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        // Prev
        IconButton(onPrev, Modifier.size(58.dp)) {
            Icon(Icons.Rounded.SkipPrevious, null, Modifier.size(36.dp))
        }
        // Big play/pause
        BigPlayBtn(state.isPlaying, state.isBuffering, onClick = onPlayPause, size = 74.dp)

        // Next
        IconButton(onNext, Modifier.size(58.dp)) {
            Icon(Icons.Rounded.SkipNext, null, Modifier.size(36.dp))
        }
        // Repeat
        IconButton(onRepeat, Modifier.size(50.dp)) {
            Icon(
                if (state.repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                null, Modifier.size(22.dp),
                tint = if (state.repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Queue row ─────────────────────────────────────────────────────────────────
@Composable
private fun QueueRow(item: MediaItem, pos: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("$pos", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(22.dp))
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(10.dp))) {
            if (item.isAD17) AD17ThumbnailImage(item.path, Modifier.fillMaxSize())
            else AlbumArt(item.albumArtUri, false, Modifier.fillMaxSize())
        }
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${item.displayArtist} · ${item.durationFormatted}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
    HorizontalDivider(Modifier.padding(start = 80.dp), 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(0.2f))
}

// ── Shared helpers ────────────────────────────────────────────────────────────
@Composable
private fun BigPlayBtn(
    isPlaying: Boolean, isBuffering: Boolean,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 76.dp
) {
    val scale by animateFloatAsState(if (isPlaying) 1f else 0.92f, spring(Spring.DampingRatioMediumBouncy), label = "bigPP")
    Box(
        Modifier.size(size).scale(scale)
            .shadow(if (isPlaying) 20.dp else 6.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Indigo, MaterialTheme.colorScheme.secondary)))
            .clickable(onClick = onClick),
        Alignment.Center
    ) {
        AnimatedContent(isPlaying, label = "bpp", transitionSpec = { scaleIn(tween(160)) togetherWith scaleOut(tween(130)) }) { p ->
            if (isBuffering) CircularProgressIndicator(Modifier.size(28.dp), color = Color.White, strokeWidth = 3.dp)
            else Icon(if (p) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(size * 0.5f))
        }
    }
}

@Composable
private fun GlassIconBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Box(
        Modifier.size(size).clip(CircleShape).background(Color.White.copy(0.16f)).clickable(onClick = onClick),
        Alignment.Center
    ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(iconSize)) }
}

// ── Sleep Timer Dialog ────────────────────────────────────────────────────────
@Composable
private fun SleepTimerDialog(active: Boolean, onDismiss: () -> Unit, onSet: (Long) -> Unit, onCancel: () -> Unit) {
    val options = listOf("15 min" to 15L, "30 min" to 30L, "45 min" to 45L, "60 min" to 60L, "90 min" to 90L)
    AlertDialog(
        onDismissRequest = onDismiss,
        title  = { Text("Sleep Timer", style = MaterialTheme.typography.titleLarge) },
        text   = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (lbl, min) ->
                    OutlinedButton(onClick = { onSet(min * 60_000L) }, modifier = Modifier.fillMaxWidth()) { Text(lbl) }
                }
                if (active) {
                    Spacer(Modifier.height(2.dp))
                    Button(onClick = onCancel, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("Cancel Timer")
                    }
                }
            }
        },
        confirmButton  = {},
        dismissButton  = { TextButton(onDismiss) { Text("Dismiss") } },
        shape          = RoundedCornerShape(24.dp)
    )
}

private fun fmtMs(ms: Long): String {
    val s = ms / 1000
    return if (s >= 3600) "%d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60) else "%d:%02d".format(s / 60, s % 60)
}
