package com.auralyx.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.auralyx.domain.model.MediaItem
import com.auralyx.ui.theme.*
import com.auralyx.utils.ThumbnailUtils

// ─────────────────────────────────────────────────────────────────────────────
// CARD (horizontal carousel)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MediaCard(item: MediaItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val src     = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale   by animateFloatAsState(
        if (pressed) 0.94f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "cardScale"
    )
    val elevation by animateDpAsState(if (pressed) 4.dp else 14.dp, tween(180), label = "cardElev")

    Box(
        modifier = modifier
            .width(160.dp)
            .scale(scale)
            .shadow(elevation, RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .clickable(src, null, onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column {
            // Artwork
            Box(Modifier.fillMaxWidth().height(160.dp)) {
                when {
                    item.isAD17 -> AD17ThumbnailImage(item.path, Modifier.fillMaxSize())
                    else        -> AlbumArt(item.albumArtUri, false, Modifier.fillMaxSize())
                }
                // Bottom scrim
                Box(Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1.00f to Color(0xBB000000)
                    )
                ))
                // aD17 badge
                if (item.isAD17) {
                    AD17Badge(Modifier.align(Alignment.TopEnd).padding(8.dp))
                }
                // Play button
                Box(
                    Modifier.size(36.dp).align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 8.dp)
                        .clip(CircleShape).background(Indigo.copy(0.9f)),
                    Alignment.Center
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            // Text
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    item.displayArtist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LIST ROW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MediaListItem(
    item: MediaItem,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    val src     = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val bg by animateColorAsState(
        if (pressed) MaterialTheme.colorScheme.primary.copy(0.08f) else Color.Transparent,
        tween(120), label = "rowBg"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(src, null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Artwork
        Box(Modifier.size(52.dp)) {
            val shape = RoundedCornerShape(12.dp)
            if (item.isAD17)
                AD17ThumbnailImage(item.path, Modifier.fillMaxSize().clip(shape))
            else
                AlbumArt(item.albumArtUri, false, Modifier.fillMaxSize().clip(shape))

            if (isPlaying) {
                Box(
                    Modifier.fillMaxSize().clip(shape).background(Indigo.copy(0.55f)),
                    Alignment.Center
                ) { PlayingBars() }
            }
        }
        // Text
        Column(Modifier.weight(1f)) {
            Text(
                item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (item.isAD17) AD17Badge()
                Text(
                    "${item.displayArtist} · ${item.durationFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing?.invoke()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// aD17 BADGE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AD17Badge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Indigo.copy(0.88f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            Arrangement.spacedBy(3.dp),
            Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Videocam, null, tint = Color.White, modifier = Modifier.size(9.dp))
            Text("aD17", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 9.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ALBUM ART
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AlbumArt(uri: String?, isVideo: Boolean = false, modifier: Modifier = Modifier) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        if (!uri.isNullOrBlank()) {
            AsyncImage(uri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.linearGradient(
                        listOf(Indigo.copy(0.28f), Rose.copy(0.15f))
                    )
                ),
                Alignment.Center
            ) {
                Icon(
                    if (isVideo) Icons.Default.Videocam else Icons.Default.MusicNote,
                    null,
                    tint = MaterialTheme.colorScheme.primary.copy(0.6f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// aD17 THUMBNAIL (cached)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AD17ThumbnailImage(path: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bmp     by remember(path) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(path) { bmp = ThumbnailUtils.getAD17Thumbnail(context, path) }
    if (bmp != null) {
        Image(bmp!!.asImageBitmap(), null, modifier = modifier, contentScale = ContentScale.Crop)
    } else {
        Box(
            modifier.background(Brush.linearGradient(listOf(Indigo.copy(0.35f), Rose.copy(0.18f)))),
            Alignment.Center
        ) {
            Icon(Icons.Default.Videocam, null, tint = Indigo.copy(0.55f), modifier = Modifier.size(28.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PLAYING BARS (animated equalizer)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PlayingBars(modifier: Modifier = Modifier, color: Color = Color.White) {
    Row(modifier.height(16.dp), Arrangement.spacedBy(2.dp), Alignment.Bottom) {
        listOf(280, 380, 240).forEachIndexed { i, dur ->
            val inf = rememberInfiniteTransition(label = "bar$i")
            val h by inf.animateFloat(
                0.2f, 1f,
                infiniteRepeatable(tween(dur, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "h$i"
            )
            Box(Modifier.width(3.dp).fillMaxHeight(h).background(color, RoundedCornerShape(1.5.dp)))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SHIMMER skeleton
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "shimmer")
    val x by inf.animateFloat(0f, 1000f, infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart), label = "sx")
    Box(
        modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    MaterialTheme.colorScheme.surfaceVariant
                ),
                start = androidx.compose.ui.geometry.Offset(x - 300f, 0f),
                end   = androidx.compose.ui.geometry.Offset(x + 300f, 0f)
            )
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// PERMISSION SCREEN (rebuilt)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PermissionScreen(onGrant: () -> Unit) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Box(
                Modifier.size(96.dp).clip(CircleShape).background(Indigo.copy(0.15f)),
                Alignment.Center
            ) {
                Icon(Icons.Default.LibraryMusic, null, tint = Indigo, modifier = Modifier.size(48.dp))
            }
            Text("Welcome to Auralyx", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text(
                "To discover your music, Auralyx needs access to your storage.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onGrant,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo)
            ) {
                Icon(Icons.Default.LibraryMusic, null, tint = Color.White)
                Spacer(Modifier.width(10.dp))
                Text("Grant Access", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}
