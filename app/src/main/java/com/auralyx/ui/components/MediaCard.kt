package com.auralyx.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.auralyx.domain.model.MediaItem
import com.auralyx.ui.theme.PurpleAccent
import com.auralyx.utils.ThumbnailUtils

/** Horizontal card for home carousels. Loads aD17 thumbnails from disk cache. */
@Composable
fun MediaCard(item: MediaItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val src     = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale   by animateFloatAsState(if (pressed) 0.93f else 1f, spring(stiffness = Spring.StiffnessMediumLow), label = "s")

    Card(
        modifier  = modifier.width(165.dp).scale(scale).clickable(src, null, onClick = onClick),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (pressed) 2.dp else 8.dp)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(165.dp)) {
                if (item.isAD17) {
                    AD17ThumbnailImage(item.path, Modifier.fillMaxSize())
                } else {
                    AlbumArt(item.albumArtUri, false, Modifier.fillMaxSize())
                }
                // Bottom gradient scrim
                Box(Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(0.6f)))
                ))
                // aD17 badge
                if (item.isAD17) {
                    Surface(Modifier.padding(8.dp).align(Alignment.TopEnd), color=PurpleAccent.copy(0.92f), shape=RoundedCornerShape(8.dp)) {
                        Row(Modifier.padding(horizontal=6.dp,vertical=3.dp), Arrangement.spacedBy(3.dp), Alignment.CenterVertically) {
                            Icon(Icons.Default.Videocam, null, tint=Color.White, modifier=Modifier.size(11.dp))
                            Text("aD17", style=MaterialTheme.typography.labelSmall, color=Color.White)
                        }
                    }
                }
                Icon(Icons.Default.PlayCircle, null, tint=Color.White.copy(0.85f),
                    modifier=Modifier.size(32.dp).align(Alignment.BottomEnd).padding(end=8.dp, bottom=8.dp))
            }
            Column(Modifier.padding(horizontal=13.dp, vertical=10.dp)) {
                Text(item.title, style=MaterialTheme.typography.labelLarge, fontWeight=FontWeight.SemiBold, maxLines=1, overflow=TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(item.displayArtist, style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant, maxLines=1, overflow=TextOverflow.Ellipsis)
            }
        }
    }
}

/** List row for songs/library/search. */
@Composable
fun MediaListItem(
    item: MediaItem, isPlaying: Boolean = false,
    onClick: () -> Unit, modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    ListItem(
        modifier = modifier.clickable(onClick=onClick),
        headlineContent = {
            Text(item.title, style=MaterialTheme.typography.titleSmall,
                color=if(isPlaying)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines=1, overflow=TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text("${item.displayArtist} \u2022 ${item.durationFormatted}", style=MaterialTheme.typography.bodySmall,
                color=MaterialTheme.colorScheme.onSurfaceVariant, maxLines=1, overflow=TextOverflow.Ellipsis)
        },
        leadingContent = {
            Box(Modifier.size(50.dp)) {
                if (item.isAD17) AD17ThumbnailImage(item.path, Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)))
                else AlbumArt(item.albumArtUri, false, Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)))
                if (isPlaying) {
                    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary.copy(0.45f)), Alignment.Center) {
                        PlayingBars()
                    }
                }
            }
        },
        trailingContent = trailing
    )
}

/** Loads and shows a cached video-frame bitmap for .aD17 files. */
@Composable
fun AD17ThumbnailImage(path: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bmp     by remember(path) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(path) { bmp = ThumbnailUtils.getAD17Thumbnail(context, path) }
    if (bmp != null) {
        Image(bmp!!.asImageBitmap(), null, modifier=modifier, contentScale=ContentScale.Crop)
    } else {
        Box(modifier.background(Brush.linearGradient(listOf(Color(0xFF2D1B69),Color(0xFF0F0730)))), Alignment.Center) {
            Icon(Icons.Default.Videocam, null, tint=PurpleAccent.copy(0.6f), modifier=Modifier.size(28.dp))
        }
    }
}

/** Standard album art via Coil. */
@Composable
fun AlbumArt(uri: String?, isVideo: Boolean = false, modifier: Modifier = Modifier) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        if (!uri.isNullOrBlank()) {
            AsyncImage(uri, null, Modifier.fillMaxSize(), contentScale=ContentScale.Crop)
        } else {
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(
                MaterialTheme.colorScheme.primary.copy(0.35f), MaterialTheme.colorScheme.tertiary.copy(0.18f)
            ))), Alignment.Center) {
                Icon(if(isVideo)Icons.Default.Videocam else Icons.Default.MusicNote, null,
                    tint=MaterialTheme.colorScheme.primary.copy(0.7f), modifier=Modifier.size(36.dp))
            }
        }
    }
}

/** Three animated equalizer bars. */
@Composable
fun PlayingBars(modifier: Modifier = Modifier) {
    Row(modifier.height(18.dp), Arrangement.spacedBy(2.dp), Alignment.Bottom) {
        listOf(300,380,260).forEachIndexed { i, dur ->
            val inf = rememberInfiniteTransition(label="b$i")
            val h by inf.animateFloat(0.2f, 1f, infiniteRepeatable(tween(dur), RepeatMode.Reverse), label="h$i")
            Box(Modifier.width(3.dp).fillMaxHeight(h).background(Color.White, RoundedCornerShape(2.dp)))
        }
    }
}
