package com.auralyx.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

import androidx.compose.foundation.background
import androidx.compose.foundation.blur
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.rounded.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

import kotlinx.coroutines.delay


@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    vm: PlayerViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val activity = LocalContext.current as? Activity

    DisposableEffect(state.isVideoEnabled, state.isPlaying) {
        if (state.isVideoEnabled && state.isPlaying) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(state.currentItem?.id) {
        state.currentItem?.id?.let(vm::updateLastPlayed)
    }

    if (state.isVideoEnabled && state.currentItem?.isAD17 == true) {
        VideoPlayerLayout(
            state = state,
            vm = vm,
            activity = activity,
            onBack = onBack
        )
    } else {
        AudioPlayerLayout(
            state = state,
            vm = vm,
            onBack = onBack
        )
    }
}

@Composable
private fun VideoPlayerLayout(
    state: PlayerState,
    vm: PlayerViewModel,
    activity: Activity?,
    onBack: () -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }

    LaunchedEffect(showControls, state.isPlaying) {
        if (showControls && state.isPlaying) {
            delay(3500)
            showControls = false
        }
    }

    DisposableEffect(isFullscreen, activity) {
        activity?.requestedOrientation =
            if (isFullscreen) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {
                    showControls = !showControls
                }
            }
    ) {

        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setBackgroundColor(android.graphics.Color.BLACK)
                    player = vm.player.exoPlayer
                }
            },
            update = {
                it.player = vm.player.exoPlayer
            },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            VideoOverlay(
                state = state,
                vm = vm,
                isFullscreen = isFullscreen,
                onBack = onBack,
                onToggleFs = {
                    isFullscreen = !isFullscreen
                }
            )
        }
    }
}

@Composable
private fun VideoOverlay(
    state: PlayerState,
    vm: PlayerViewModel,
    isFullscreen: Boolean,
    onBack: () -> Unit,
    onToggleFs: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.72f),
                    0.28f to Color.Transparent,
                    0.72f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.88f)
                )
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
            ) {
                Text(
                    text = state.currentItem?.title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = state.currentItem?.displayArtist.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { vm.toggleVideo() }) {
                Icon(
                    imageVector = Icons.Rounded.AudioFile,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            IconButton(onClick = onToggleFs) {
                Icon(
                    imageVector = if (isFullscreen) {
                        Icons.Rounded.FullscreenExit
                    } else {
                        Icons.Rounded.Fullscreen
                    },
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconBtn(
                icon = Icons.Rounded.Replay10,
                size = 48.dp,
                iconSize = 28.dp
            ) {
                vm.seekTo((state.progress - 10_000).coerceAtLeast(0))
            }

            GlassIconBtn(
                icon = Icons.Rounded.SkipPrevious,
                size = 54.dp,
                iconSize = 32.dp
            ) {
                vm.skipToPrev()
            }

            BigPlayBtn(
                isPlaying = state.isPlaying,
                isBuffering = state.isBuffering,
                onClick = { vm.togglePlayPause() }
            )

            GlassIconBtn(
                icon = Icons.Rounded.SkipNext,
                size = 54.dp,
                iconSize = 32.dp
            ) {
                vm.skipToNext()
            }

            GlassIconBtn(
                icon = Icons.Rounded.Forward10,
                size = 48.dp,
                iconSize = 28.dp
            ) {
                vm.seekTo((state.progress + 10_000).coerceAtMost(state.duration))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = fmtMs(state.progress),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Text(
                    text = state.currentItem?.durationFormatted ?: "--:--",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Slider(
                value = state.progressFraction,
                onValueChange = vm::seekToFraction,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Indigo,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun AudioPlayerLayout(
    state: PlayerState,
    vm: PlayerViewModel,
    onBack: () -> Unit
) {
    val queueListState = rememberLazyListState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        state.currentItem?.let { item ->

            if (item.isAD17) {
                AD17ThumbnailImage(
                    item.path,
                    Modifier
                        .fillMaxSize()
                        .blur(80.dp)
                        .alpha(0.25f)
                )
            } else if (!item.albumArtUri.isNullOrBlank()) {
                AsyncImage(
                    model = item.albumArtUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(80.dp)
                        .alpha(0.25f),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.background.copy(alpha = 0.82f),
                        0.4f to MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                        1f to MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
                    )
                )
        )

        LazyColumn(
            state = queueListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            item {
                PlayerArtwork(state = state)
            }

            item {
                PlayerSongInfo(state = state)
            }

            item {
                PlayerSeekBar(
                    state = state,
                    onSeek = vm::seekToFraction
                )
            }

            item {
                PlayerControls(
                    state = state,
                    onPlayPause = vm::togglePlayPause,
                    onPrev = vm::skipToPrev,
                    onNext = vm::skipToNext,
                    onShuffle = vm::toggleShuffle,
                    onRepeat = vm::cycleRepeatMode
                )
            }
        }
    }
}

@Composable
private fun PlayerArtwork(
    state: PlayerState,
    modifier: Modifier = Modifier
) {
    val artScale by animateFloatAsState(
        targetValue = if (state.isPlaying) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "artScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .aspectRatio(1f)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(1.05f)
                .blur(48.dp)
                .background(
                    color = IndigoGlow.copy(
                        alpha = if (state.isPlaying) 0.5f else 0.25f
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(artScale)
                .shadow(
                    elevation = if (state.isPlaying) 40.dp else 12.dp,
                    shape = RoundedCornerShape(28.dp)
                )
                .clip(RoundedCornerShape(28.dp))
        ) {

            when {
                state.currentItem?.isAD17 == true -> {
                    AD17ThumbnailImage(
                        state.currentItem!!.path,
                        Modifier.fillMaxSize()
                    )
                }

                else -> {
                    AlbumArt(
                        uri = state.currentItem?.albumArtUri,
                        showPlaceholder = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerSongInfo(state: PlayerState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 8.dp)
    ) {

        Text(
            text = state.currentItem?.title ?: "Nothing playing",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = state.currentItem?.displayArtist ?: "—",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlayerSeekBar(
    state: PlayerState,
    onSeek: (Float) -> Unit
) {
    var sliderPosition by remember(state.progressFraction) {
        mutableFloatStateOf(state.progressFraction)
    }

    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {

        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
            },
            onValueChangeFinished = {
                onSeek(sliderPosition)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = fmtMs(state.progress),
                style = MaterialTheme.typography.labelSmall
            )

            Text(
                text = state.currentItem?.durationFormatted ?: "--:--",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = onShuffle) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = null,
                tint = if (state.shuffleEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        IconButton(onClick = onPrev) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }

        BigPlayBtn(
            isPlaying = state.isPlaying,
            isBuffering = state.isBuffering,
            onClick = onPlayPause
        )

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(onClick = onRepeat) {
            Icon(
                imageVector = if (state.repeatMode == RepeatMode.ONE) {
                    Icons.Rounded.RepeatOne
                } else {
                    Icons.Rounded.Repeat
                },
                contentDescription = null,
                tint = if (state.repeatMode != RepeatMode.OFF) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun BigPlayBtn(
    isPlaying: Boolean,
    isBuffering: Boolean,
    onClick: () -> Unit,
    size: Dp = 76.dp
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "bigPP"
    )

    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .shadow(
                elevation = if (isPlaying) 20.dp else 6.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Indigo,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {

        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                scaleIn(animationSpec = tween(160)) togetherWith
                        scaleOut(animationSpec = tween(130))
            },
            label = "bpp"
        ) { playing ->

            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = if (playing) {
                        Icons.Rounded.Pause
                    } else {
                        Icons.Rounded.PlayArrow
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
    }
}

@Composable
private fun GlassIconBtn(
    icon: ImageVector,
    size: Dp,
    iconSize: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.16f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

private fun fmtMs(ms: Long): String {
    val totalSeconds = ms / 1000

    return if (totalSeconds >= 3600) {
        "%d:%02d:%02d".format(
            totalSeconds / 3600,
            (totalSeconds % 3600) / 60,
            totalSeconds % 60
        )
    } else {
        "%d:%02d".format(
            totalSeconds / 60,
            totalSeconds % 60
        )
    }
}
