package com.auralyx.ui.library

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auralyx.domain.model.*
import com.auralyx.ui.components.*
import com.auralyx.ui.theme.*

@Composable
fun LibraryScreen(onNavigateToPlayer: () -> Unit, vm: LibraryViewModel = hiltViewModel()) {
    val state  by vm.state.collectAsState()
    val player by vm.playerState.collectAsState()

    Column(Modifier.fillMaxSize()) {
        // ── Header ─────────────────────────────────────────────────────────
        Column(
            Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Library",
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                modifier   = Modifier.padding(top = 16.dp, bottom = 12.dp)
            )

            val tabs   = LibraryTab.values().toList()
            val labels = listOf("Songs", "Albums", "Artists", "Folders", "Videos")

            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(state.tab).coerceAtLeast(0),
                edgePadding      = 0.dp,
                containerColor   = Color.Transparent,
                divider          = {},
                indicator        = { positions ->
                    val i = tabs.indexOf(state.tab)
                    if (i in positions.indices) {
                        Box(
                            Modifier
                                .tabIndicatorOffset(positions[i])
                                .height(3.dp)
                                .padding(horizontal = 14.dp)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { i, t ->
                    Tab(
                        selected = state.tab == t,
                        onClick  = { vm.selectTab(t) },
                        text = {
                            Text(
                                labels[i],
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = if (state.tab == t) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.15f))
        }

        // ── Tab content ────────────────────────────────────────────────────
        AnimatedContent(
            targetState  = state.tab,
            modifier     = Modifier.weight(1f).fillMaxWidth(),
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                (fadeIn(tween(220)) + slideInHorizontally(tween(260)) { if (forward) 60 else -60 }) togetherWith
                (fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { if (forward) -60 else 60 })
            },
            label = "libTab"
        ) { tab ->
            when (tab) {
                LibraryTab.SONGS   -> SongsList(state.songs, player.currentItem?.id, player.isPlaying) { s ->
                    vm.play(s, state.songs); onNavigateToPlayer()
                }
                LibraryTab.ALBUMS  -> AlbumsGrid(state.albums)
                LibraryTab.ARTISTS -> ArtistsList(state.artists)
                LibraryTab.FOLDERS -> FoldersList(state.folders)
                LibraryTab.VIDEOS  -> SongsList(state.videos, player.currentItem?.id, player.isPlaying, isVideo = true) { v ->
                    vm.play(v, state.videos, true); onNavigateToPlayer()
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SongsList(
    songs: List<MediaItem>,
    playingId: Long?,
    isPlaying: Boolean,
    isVideo: Boolean = false,
    onClick: (MediaItem) -> Unit
) {
    if (songs.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(if (isVideo) Icons.Outlined.Videocam else Icons.Outlined.MusicNote, null, Modifier.size(52.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f))
                Text("No ${if (isVideo) "videos" else "songs"} found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(songs, key = { it.id }) { song ->
            MediaListItem(
                item      = song,
                isPlaying = playingId == song.id && isPlaying,
                onClick   = { onClick(song) },
                modifier  = Modifier.animateItem()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AlbumsGrid(albums: List<Album>) {
    if (albums.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No albums", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyVerticalGrid(
        columns               = GridCells.Adaptive(160.dp),
        contentPadding        = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement   = Arrangement.spacedBy(12.dp)
    ) {
        items(albums.size, key = { albums[it].id }) { i -> AlbumCard(albums[i], Modifier.animateItem()) }
    }
}

@Composable
private fun AlbumCard(album: Album, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable {}
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
            AlbumArt(album.artUri, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(
                Brush.verticalGradient(0.6f to Color.Transparent, 1.0f to Color.Black.copy(0.5f))
            ))
            Text(
                "${album.songCount}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(0.9f),
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    .background(Color.Black.copy(0.42f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Column(Modifier.padding(10.dp)) {
            Text(album.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(album.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ArtistsList(artists: List<Artist>) {
    if (artists.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No artists", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(artists.size, key = { artists[it].id }) { i ->
            val a = artists[i]
            ListItem(
                headlineContent   = { Text(a.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium) },
                supportingContent = { Text("${a.albumCount} albums · ${a.songCount} songs", style = MaterialTheme.typography.bodySmall) },
                leadingContent    = {
                    Box(Modifier.size(50.dp).clip(CircleShape)) {
                        AlbumArt(a.artUri, modifier = Modifier.fillMaxSize())
                    }
                }
            )
            HorizontalDivider(Modifier.padding(start = 72.dp), 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(0.15f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FoldersList(folders: List<Folder>) {
    if (folders.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No folders", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(folders.size, key = { folders[it].id }) { i ->
            val f = folders[i]
            ListItem(
                headlineContent   = { Text(f.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium) },
                supportingContent = { Text(f.path, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                trailingContent   = { Text("${f.songCount}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) },
                leadingContent    = {
                    Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(Indigo.copy(0.14f)), Alignment.Center) {
                        Icon(Icons.Rounded.Folder, null, tint = Indigo, modifier = Modifier.size(28.dp))
                    }
                }
            )
            HorizontalDivider(Modifier.padding(start = 72.dp), 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(0.15f))
        }
    }
}
