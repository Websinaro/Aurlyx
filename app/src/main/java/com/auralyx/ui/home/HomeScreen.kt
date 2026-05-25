package com.auralyx.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.auralyx.domain.model.MediaItem
import com.auralyx.ui.components.*
import com.auralyx.ui.theme.*
import java.util.Calendar

@Composable
fun HomeScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val state  by vm.uiState.collectAsState()
    val player by vm.playerState.collectAsState()
    val listState = rememberLazyListState()

    // Top-bar alpha based on scroll
    val topAlpha by remember {
        derivedStateOf {
            val first = listState.layoutInfo.visibleItemsInfo.firstOrNull() ?: return@derivedStateOf 0f
            (((-first.offset - 60f) / 120f)).coerceIn(0f, 1f)
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Translucent blurred header that appears on scroll
        Box(
            Modifier.fillMaxWidth().height(80.dp).align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background.copy(topAlpha * 0.96f),
                            MaterialTheme.colorScheme.background.copy(0f)
                        )
                    )
                )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ── Header
            item {
                HomeHeader(
                    isScanning          = state.isScanning,
                    onSettings          = onNavigateToSettings,
                    onScan              = { vm.scanStorage() }
                )
            }

            // ── Hero / Now Playing hero card
            if (player.currentItem != null) item(key = "hero") {
                NowPlayingHeroCard(
                    item     = player.currentItem!!,
                    isPlaying = player.isPlaying,
                    onClick  = onNavigateToPlayer,
                    modifier = Modifier.animateItem()
                )
            }

            // ── Shuffle banner
            if (state.allSongs.isNotEmpty()) item(key = "shuffle") {
                ShuffleBanner(
                    count   = state.allSongs.size,
                    onClick = {
                        val shuffled = state.allSongs.shuffled()
                        vm.play(shuffled.first(), shuffled)
                        onNavigateToPlayer()
                    },
                    modifier = Modifier.animateItem()
                )
            }

            // ── Favorites
            if (state.favorites.isNotEmpty()) {
                item(key = "fav_hdr") { SectionHeader("Favorites ❤") }
                item(key = "fav_row") {
                    Carousel(state.favorites) { item ->
                        vm.play(item, state.favorites)
                        onNavigateToPlayer()
                    }
                }
            }

            // ── Recently played
            if (state.recentlyPlayed.isNotEmpty()) {
                item(key = "rec_hdr") { SectionHeader("Recently Played") }
                item(key = "rec_row") {
                    Carousel(state.recentlyPlayed) { item ->
                        vm.play(item, state.recentlyPlayed)
                        onNavigateToPlayer()
                    }
                }
            }

            // ── Most played
            if (state.mostPlayed.isNotEmpty()) {
                item(key = "most_hdr") { SectionHeader("Most Played") }
                item(key = "most_row") {
                    Carousel(state.mostPlayed) { item ->
                        vm.play(item, state.mostPlayed)
                        onNavigateToPlayer()
                    }
                }
            }

            // ── Music videos
            if (state.musicVideos.isNotEmpty()) {
                item(key = "vid_hdr") { SectionHeader("Music Videos") }
                item(key = "vid_row") {
                    Carousel(state.musicVideos) { item ->
                        vm.play(item, state.musicVideos, true)
                        onNavigateToPlayer()
                    }
                }
            }

            // ── Songs
            if (state.allSongs.isNotEmpty()) {
                item(key = "songs_hdr") {
                    SectionHeader("Songs", actionLabel = "See all")
                }
                items(state.allSongs.take(20), key = { "s_${it.id}" }) { song ->
                    MediaListItem(
                        item      = song,
                        isPlaying = player.currentItem?.id == song.id && player.isPlaying,
                        onClick   = { vm.play(song, state.allSongs); onNavigateToPlayer() },
                        modifier  = Modifier.animateItem()
                    )
                }
            }

            // ── Empty state
            if (state.allSongs.isEmpty() && !state.isLoading && !state.isScanning) item(key = "empty") {
                EmptyState(onScan = { vm.scanStorage() })
            }

            // ── Loading state
            if (state.isLoading) item(key = "loading") {
                LoadingSkeletons()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HomeHeader(isScanning: Boolean, onSettings: () -> Unit, onScan: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        Column {
            Text(
                greeting(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Auralyx",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            AnimatedContent(isScanning, label = "scan", transitionSpec = {
                scaleIn(tween(180)) togetherWith scaleOut(tween(140))
            }) { scanning ->
                if (scanning) {
                    IconButton(onClick = {}) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    }
                } else {
                    IconButton(onClick = onScan) {
                        Icon(Icons.Outlined.Refresh, "Scan", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Outlined.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NowPlayingHeroCard(
    item: MediaItem,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        // Blurred album art background
        val src = remember { MutableInteractionSource() }
        val src2 = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

        if (item.isAD17) AD17ThumbnailImage(item.path, Modifier.fillMaxSize().blur(24.dp).alpha(0.6f))
        else if (!item.albumArtUri.isNullOrBlank()) {
            AsyncImage(item.albumArtUri, null, Modifier.fillMaxSize().blur(24.dp).alpha(0.6f), contentScale = ContentScale.Crop)
        } else {
            Box(Modifier.fillMaxSize().height(100.dp).background(Brush.linearGradient(listOf(Indigo.copy(0.35f), Rose.copy(0.22f)))))
        }

        // Scrim
        Box(Modifier.matchParentSize().background(
            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceContainer.copy(0.7f), MaterialTheme.colorScheme.surfaceContainerHigh.copy(0.92f)))
        ))

        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Thumb
            Box(Modifier.size(60.dp).clip(RoundedCornerShape(14.dp))) {
                if (item.isAD17) AD17ThumbnailImage(item.path, Modifier.fillMaxSize())
                else AlbumArt(item.albumArtUri, false, Modifier.fillMaxSize())
            }
            Column(Modifier.weight(1f)) {
                Text("NOW PLAYING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(2.dp))
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.displayArtist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (isPlaying) PlayingBars(Modifier.padding(end = 4.dp), color = MaterialTheme.colorScheme.primary)
            else Icon(Icons.Rounded.PlayArrow, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ShuffleBanner(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "shimmerBanner")
    val gx by inf.animateFloat(0f, 800f, infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse), label = "gx")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Indigo, Rose, Indigo.copy(0.8f)),
                    start  = androidx.compose.ui.geometry.Offset(gx, 0f),
                    end    = androidx.compose.ui.geometry.Offset(gx + 400f, 80f)
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(0.18f)), Alignment.Center) {
                Icon(Icons.Rounded.Shuffle, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Column {
                Text("Shuffle All", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text("$count songs", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.82f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun Carousel(items: List<MediaItem>, onItemClick: (MediaItem) -> Unit) {
    LazyRow(
        contentPadding       = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { item ->
            MediaCard(item = item, onClick = { onItemClick(item) })
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EmptyState(onScan: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(60.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val inf = rememberInfiniteTransition(label = "emptyPulse")
        val scale by inf.animateFloat(0.9f, 1.05f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ep")
        Box(
            Modifier.size(100.dp).scale(scale).clip(CircleShape)
                .background(Brush.radialGradient(listOf(Indigo.copy(0.22f), Indigo.copy(0f)))),
            Alignment.Center
        ) {
            Icon(Icons.Outlined.MusicNote, null, tint = Indigo, modifier = Modifier.size(52.dp))
        }
        Text("No music found", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Tap the button below to scan your storage for music and videos.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Button(
            onClick = onScan,
            shape   = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors  = ButtonDefaults.buttonColors(containerColor = Indigo)
        ) {
            Icon(Icons.Default.Search, null)
            Spacer(Modifier.width(8.dp))
            Text("Scan Storage", style = MaterialTheme.typography.titleSmall)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LoadingSkeletons() {
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Carousel shimmer
        Row(Modifier.padding(horizontal = 20.dp), Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                ShimmerBox(Modifier.size(160.dp).clip(RoundedCornerShape(20.dp)))
            }
        }
        // List shimmer
        repeat(5) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), Arrangement.spacedBy(14.dp), Alignment.CenterVertically) {
                ShimmerBox(Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShimmerBox(Modifier.fillMaxWidth(0.65f).height(14.dp).clip(RoundedCornerShape(7.dp)))
                    ShimmerBox(Modifier.fillMaxWidth(0.45f).height(11.dp).clip(RoundedCornerShape(5.dp)))
                }
            }
        }
    }
}

private fun greeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when { h < 12 -> "Good morning"; h < 18 -> "Good afternoon"; else -> "Good evening" }
}
