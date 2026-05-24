package com.auralyx.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auralyx.domain.model.*
import com.auralyx.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onNavigateToPlayer: () -> Unit, vm: LibraryViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val player by vm.playerState.collectAsState()
    val tabs = LibraryTab.values().toList()
    val labels = listOf("Songs", "Albums", "Artists", "Folders", "Videos")

    GradientBackground {
        Scaffold(containerColor = Color.Transparent, topBar = {
            Column {
                TopAppBar(title = { Text("Library", style = MaterialTheme.typography.headlineLarge) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent))
                ScrollableTabRow(
                    selectedTabIndex = tabs.indexOf(state.tab).coerceAtLeast(0),
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    indicator = { pos ->
                        val i = tabs.indexOf(state.tab)
                        if (i != -1 && i < pos.size) {
                            TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(pos[i]), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                ) {
                    tabs.forEachIndexed { i, t -> Tab(selected = state.tab == t, onClick = { vm.selectTab(t) }, text = { Text(labels[i]) }) }
                }
            }
        }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (state.isLoading) CircularProgressIndicator(Modifier.align(Alignment.Center))
                else when (state.tab) {
                    LibraryTab.SONGS -> LazyColumn { items(state.songs, key = { it.id }) { s -> MediaListItem(s, isPlaying = player.currentItem?.id == s.id && player.isPlaying, onClick = { vm.play(s, state.songs); onNavigateToPlayer() }); HorizontalDivider(Modifier.padding(start = 72.dp), 0.5.dp) } }
                    LibraryTab.ALBUMS -> LazyVerticalGrid(GridCells.Adaptive(160.dp), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.albums.size) { i ->
                            val a = state.albums[i]
                            Card(shape = RoundedCornerShape(12.dp)) {
                                Column {
                                    AlbumArt(a.artUri, modifier = Modifier.fillMaxWidth().aspectRatio(1f))
                                    Column(Modifier.padding(8.dp)) {
                                        Text(a.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${a.songCount} songs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                    LibraryTab.ARTISTS -> LazyColumn {
                        items(state.artists.size) { i ->
                            val a = state.artists[i]
                            ListItem(headlineContent = { Text(a.name) }, supportingContent = { Text("${a.albumCount} albums • ${a.songCount} songs", style = MaterialTheme.typography.bodySmall) }, leadingContent = { Box(Modifier.size(48.dp)) { AlbumArt(a.artUri, modifier = Modifier.fillMaxSize().clip(CircleShape)) } })
                            HorizontalDivider(Modifier.padding(start = 72.dp), 0.5.dp)
                        }
                    }
                    LibraryTab.FOLDERS -> LazyColumn { items(state.folders.size) { i -> val f = state.folders[i]; ListItem(headlineContent = { Text(f.name) }, supportingContent = { Text("${f.songCount} songs", style = MaterialTheme.typography.bodySmall) }, leadingContent = { Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp)) }) } }
                    LibraryTab.VIDEOS -> LazyColumn { items(state.videos, key = { it.id }) { v -> MediaListItem(v, isPlaying = player.currentItem?.id == v.id && player.isPlaying, onClick = { vm.play(v, state.videos, true); onNavigateToPlayer() }); HorizontalDivider(Modifier.padding(start = 72.dp), 0.5.dp) } }
                }
            }
        }
    }
}
