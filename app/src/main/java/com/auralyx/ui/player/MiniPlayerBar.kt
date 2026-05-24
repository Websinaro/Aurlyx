package com.auralyx.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auralyx.ui.components.AD17ThumbnailImage
import com.auralyx.ui.components.AlbumArt
import com.auralyx.ui.components.PlayingBars
import com.auralyx.ui.theme.PurpleAccent

/**
 * Compact player bar shown above bottom navigation.
 *  - Slides up from bottom when playback starts
 *  - Gradient animated progress strip along the top edge
 *  - aD17 thumbnail or album art
 *  - Tap anywhere → open full PlayerScreen
 */
@Composable
fun MiniPlayerBar(
    onTap: () -> Unit,
    vm: PlayerViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val item  = state.currentItem ?: return   // hidden when nothing is playing

    AnimatedVisibility(
        visible      = true,
        enter        = slideInVertically { it } + fadeIn(tween(300)),
        exit         = slideOutVertically { it } + fadeOut(tween(200))
    ) {
        Surface(
            modifier        = Modifier.fillMaxWidth().clickable(onClick = onTap),
            shape           = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color           = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation  = 16.dp,
            shadowElevation = 16.dp
        ) {
            Column {
                // ── Animated progress bar at the very top ─────────────────
                val progressAnim by animateFloatAsState(
                    state.progressFraction, tween(500), label = "mp"
                )
                Box(
                    Modifier.fillMaxWidth().height(3.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(0.08f))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(progressAnim)
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, PurpleAccent))
                            )
                    )
                }

                // ── Main content row ──────────────────────────────────────
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Artwork thumbnail
                    Box(Modifier.size(48.dp)) {
                        if (item.isAD17) {
                            AD17ThumbnailImage(item.path, Modifier.fillMaxSize().clip(RoundedCornerShape(11.dp)))
                        } else {
                            AlbumArt(item.albumArtUri, false, Modifier.fillMaxSize().clip(RoundedCornerShape(11.dp)))
                        }
                        // Equalizer overlay when playing
                        if (state.isPlaying) {
                            Box(
                                Modifier.fillMaxSize().clip(RoundedCornerShape(11.dp))
                                    .background(Color.Black.copy(0.35f)),
                                Alignment.Center
                            ) { PlayingBars() }
                        }
                    }

                    // Title + artist — fills remaining space
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.title, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            item.displayArtist, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Prev
                    IconButton(onClick = { vm.skipToPrev() }, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Default.SkipPrevious, "Prev", modifier = Modifier.size(24.dp))
                    }

                    // Play / Pause — animated scale spring
                    val btnScale by animateFloatAsState(
                        if (state.isPlaying) 1f else 0.86f,
                        spring(Spring.DampingRatioMediumBouncy),
                        label = "bs"
                    )
                    Box(
                        Modifier.size(44.dp).scale(btnScale)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { vm.togglePlayPause() },
                        Alignment.Center
                    ) {
                        AnimatedContent(
                            state.isPlaying, label = "mpp",
                            transitionSpec = { scaleIn(tween(150)) togetherWith scaleOut(tween(150)) }
                        ) { playing ->
                            if (state.isBuffering)
                                CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            else
                                Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null,
                                    tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }

                    // Next
                    IconButton(onClick = { vm.skipToNext() }, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Default.SkipNext, "Next", modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}
