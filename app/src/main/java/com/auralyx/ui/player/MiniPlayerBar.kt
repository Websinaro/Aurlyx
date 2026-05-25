package com.auralyx.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auralyx.ui.components.*
import com.auralyx.ui.theme.Indigo

@Composable
fun MiniPlayerBar(onTap: () -> Unit, vm: PlayerViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val item  = state.currentItem ?: return

    val progressAnim by animateFloatAsState(state.progressFraction, tween(600), label = "mpProg")

    Surface(
        modifier        = Modifier.fillMaxWidth().clickable(onClick = onTap),
        shape           = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        color           = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 20.dp,
        tonalElevation  = 4.dp
    ) {
        Column {
            // ── Animated progress strip ───────────────────────────────────
            Box(
                Modifier.fillMaxWidth().height(3.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(0.15f))
            ) {
                Box(
                    Modifier.fillMaxWidth(progressAnim).height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Indigo, MaterialTheme.colorScheme.secondary)
                            )
                        )
                )
            }

            // ── Content row ───────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Artwork
                Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp))) {
                    if (item.isAD17) AD17ThumbnailImage(item.path, Modifier.fillMaxSize())
                    else AlbumArt(item.albumArtUri, false, Modifier.fillMaxSize())
                    if (state.isPlaying) {
                        Box(
                            Modifier.fillMaxSize().background(Color.Black.copy(0.38f)),
                            Alignment.Center
                        ) { PlayingBars() }
                    }
                }

                // Title/artist
                Column(Modifier.weight(1f)) {
                    Text(
                        item.title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        item.displayArtist,
                        style  = MaterialTheme.typography.bodySmall,
                        color  = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Prev
                IconButton(onClick = { vm.skipToPrev() }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.SkipPrevious, "Prev", Modifier.size(24.dp))
                }

                // Play/pause
                val btnScale by animateFloatAsState(
                    if (state.isPlaying) 1f else 0.88f,
                    spring(Spring.DampingRatioMediumBouncy),
                    label = "ppScale"
                )
                Box(
                    Modifier.size(46.dp).scale(btnScale)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Indigo, MaterialTheme.colorScheme.secondary)))
                        .clickable { vm.togglePlayPause() },
                    Alignment.Center
                ) {
                    AnimatedContent(state.isPlaying, label = "mpp", transitionSpec = {
                        scaleIn(tween(160)) togetherWith scaleOut(tween(130))
                    }) { playing ->
                        if (state.isBuffering)
                            CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        else
                            Icon(
                                if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                null, tint = Color.White, modifier = Modifier.size(26.dp)
                            )
                    }
                }

                // Next
                IconButton(onClick = { vm.skipToNext() }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.SkipNext, "Next", Modifier.size(24.dp))
                }
            }
        }
    }
}
