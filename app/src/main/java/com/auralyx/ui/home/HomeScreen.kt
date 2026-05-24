package com.auralyx.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auralyx.domain.model.MediaItem
import com.auralyx.ui.components.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val state  by vm.uiState.collectAsState()
    val player by vm.playerState.collectAsState()

    GradientBackground {
        Scaffold(containerColor = Color.Transparent, topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Auralyx",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    if (state.isScanning)
                        CircularProgressIndicator(Modifier.size(20.dp).padding(end = 8.dp), strokeWidth = 2.dp)
                    else
                        IconButton(onClick = { vm.scanStorage() }) { Icon(Icons.Default.Refresh, "Scan") }
                    IconButton(onClick = onNavigateToSettings) { Icon(Icons.Outlined.Settings, "Settings") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }) { padding ->
            if (state.isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Greeting
                    item {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text(greeting(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("What's playing today?", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Shuffle banner
                    if (state.allSongs.isNotEmpty()) item {
                        ShuffleBanner(state.allSongs.size) {
                            vm.play(state.allSongs.random(), state.allSongs.shuffled())
                            onNavigateToPlayer()
                        }
                    }

                    // Favorites carousel
                    if (state.favorites.isNotEmpty()) {
                        item { SectionHeader("Favorites") }
                        item {
                            Carousel(state.favorites) {
                                vm.play(it, state.favorites)
                                onNavigateToPlayer()
                            }
                        }
                    }

                    // Recently played
                    if (state.recentlyPlayed.isNotEmpty()) {
                        item { SectionHeader("Recently Played") }
                        item {
                            Carousel(state.recentlyPlayed) {
                                vm.play(it, state.recentlyPlayed)
                                onNavigateToPlayer()
                            }
                        }
                    }

                    // Most played
                    if (state.mostPlayed.isNotEmpty()) {
                        item { SectionHeader("Most Played") }
                        item {
                            Carousel(state.mostPlayed) {
                                vm.play(it, state.mostPlayed)
                                onNavigateToPlayer()
                            }
                        }
                    }

                    // Music Videos
                    if (state.musicVideos.isNotEmpty()) {
                        item { SectionHeader("Music Videos") }
                        item {
                            Carousel(state.musicVideos) {
                                vm.play(it, state.musicVideos, true)
                                onNavigateToPlayer()
                            }
                        }
                    }

                    // Songs list (first 15)
                    if (state.allSongs.isNotEmpty()) {
                        item { SectionHeader("Songs") }
                        items(state.allSongs.take(15), key = { it.id }) { s ->
                            MediaListItem(
                                s,
                                isPlaying = player.currentItem?.id == s.id && player.isPlaying,
                                onClick = { vm.play(s, state.allSongs); onNavigateToPlayer() }
                            )
                            HorizontalDivider(Modifier.padding(start = 72.dp), 0.5.dp)
                        }
                    }

                    // Empty state
                    if (state.allSongs.isEmpty() && state.musicVideos.isEmpty()) item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.MusicNote, null, Modifier.size(72.dp), MaterialTheme.colorScheme.primary.copy(0.4f))
                            Text("No music found", style = MaterialTheme.typography.headlineSmall)
                            Button(onClick = { vm.scanStorage() }) {
                                Icon(Icons.Default.Search, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Scan Storage")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShuffleBanner(count: Int, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable(onClick = onClick),
        androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        CardDefaults.cardColors(Color.Transparent)
    ) {
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shuffle, null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Shuffle Play", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("$count songs", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
                }
            }
        }
    }
}

@Composable
private fun Carousel(items: List<MediaItem>, onItemClick: (MediaItem) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { item ->
            MediaCard(item = item, onClick = { onItemClick(item) })
        }
    }
}

private fun greeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when { h < 12 -> "Good morning,"; h < 17 -> "Good afternoon,"; else -> "Good evening," }
}
