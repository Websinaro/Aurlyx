package com.auralyx.ui.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auralyx.ui.components.*
import com.auralyx.ui.theme.Indigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onNavigateToPlayer: () -> Unit, vm: SearchViewModel = hiltViewModel()) {
    val query   by vm.query.collectAsState()
    val results by vm.results.collectAsState()
    val player  by vm.playerState.collectAsState()
    val focus   =  remember { FocusRequester() }

    LaunchedEffect(Unit) { try { focus.requestFocus() } catch (_: Exception) {} }

    Column(Modifier.fillMaxSize()) {
        // ── Header bar ────────────────────────────────────────────────────
        Column(
            Modifier.fillMaxWidth().statusBarsPadding()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Search",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            )

            // Search field
            val isFocused = query.isNotEmpty()
            TextField(
                value         = query,
                onValueChange = vm::onQueryChange,
                modifier      = Modifier.fillMaxWidth().focusRequester(focus),
                placeholder   = { Text("Songs, artists, albums…") },
                leadingIcon   = { Icon(Icons.Rounded.Search, null) },
                trailingIcon  = if (query.isNotEmpty()) {
                    { IconButton(onClick = { vm.onQueryChange("") }) { Icon(Icons.Rounded.Close, "Clear") } }
                } else null,
                singleLine    = true,
                shape         = RoundedCornerShape(18.dp),
                colors        = TextFieldDefaults.colors(
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
            Spacer(Modifier.height(8.dp))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.12f))

        // ── Results ───────────────────────────────────────────────────────
        AnimatedContent(
            targetState = Triple(query.isBlank(), results.isEmpty(), results),
            transitionSpec = {
                fadeIn(tween(220)) togetherWith fadeOut(tween(160))
            },
            label = "searchContent"
        ) { (blank, empty, items) ->
            when {
                blank -> SearchEmptyState()
                empty -> SearchNoResults(query)
                else  -> LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                    item {
                        Text(
                            "${items.size} result${if (items.size != 1) "s" else ""}",
                            style    = MaterialTheme.typography.labelMedium,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    items(items, key = { it.id }) { item ->
                        MediaListItem(
                            item      = item,
                            isPlaying = player.currentItem?.id == item.id && player.isPlaying,
                            onClick   = { vm.play(item, items); onNavigateToPlayer() },
                            modifier  = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchEmptyState() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            val inf = rememberInfiniteTransition(label = "pulse")
            val alpha by inf.animateFloat(0.4f, 0.9f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "a")
            Icon(Icons.Rounded.Search, null, Modifier.size(72.dp).alpha(alpha), Indigo.copy(0.55f))
            Text("Discover your music", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("Type to search songs, artists, and albums.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun SearchNoResults(query: String) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Icon(Icons.Outlined.SearchOff, null, Modifier.size(64.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f))
            Text("No results", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("Nothing found for \"$query\"", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
