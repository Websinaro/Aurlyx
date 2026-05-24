package com.auralyx.ui.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auralyx.ui.components.GradientBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()

    GradientBackground {
        Scaffold(containerColor = Color.Transparent, topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            ) {
                Lbl("Appearance")
                Toggle(Icons.Outlined.DarkMode, "Dark Theme", "Dark colour scheme", s.darkTheme, vm::toggleDarkTheme)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Toggle(Icons.Outlined.Palette, "Dynamic Color", "Use wallpaper colours (Android 12+)", s.dynamicColor, vm::toggleDynamicColor)
                }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Lbl("Playback")
                Toggle(Icons.Outlined.Videocam, "Default Video Mode", "Show video for .aD17 by default", s.defaultVideoMode, vm::toggleDefaultVideoMode)
                Toggle(Icons.Outlined.TravelExplore, "Scan on Launch", "Auto-scan storage on start", s.scanOnLaunch, vm::toggleScanOnLaunch)

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Lbl("Storage")
                ListItem(
                    modifier = if (!s.isScanning) Modifier.clickable { vm.scanStorage() } else Modifier,
                    headlineContent = { Text("Scan Storage") },
                    supportingContent = {
                        Text(
                            if (s.isScanning) "Scanning…" else (s.scanMsg ?: "Find music and .aD17 files"),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    leadingContent  = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = if (s.isScanning) {{ CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp) }} else null
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Lbl("About")
                ListItem(
                    headlineContent  = { Text("Auralyx Player") },
                    supportingContent = { Text("Version 2.1.0") },
                    leadingContent   = { Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary) }
                )
                ListItem(
                    headlineContent  = { Text("Supported Formats") },
                    supportingContent = { Text("MP3 • MP4 • AAC • FLAC • .aD17") },
                    leadingContent   = { Icon(Icons.Outlined.AudioFile, null, tint = MaterialTheme.colorScheme.primary) }
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun Lbl(t: String) = Text(
    t,
    style    = MaterialTheme.typography.labelMedium,
    color    = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
)

@Composable
private fun Toggle(icon: ImageVector, title: String, sub: String, checked: Boolean, onChanged: (Boolean) -> Unit) =
    ListItem(
        headlineContent  = { Text(title) },
        supportingContent = { Text(sub, style = MaterialTheme.typography.bodySmall) },
        leadingContent   = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent  = { Switch(checked = checked, onCheckedChange = onChanged) }
    )
