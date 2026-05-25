package com.auralyx.ui.settings

import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auralyx.ui.theme.Indigo

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToConverter: () -> Unit,
    vm: SettingsViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsState()

    Column(Modifier.fillMaxSize()) {
        // ── Header ────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBackIosNew, "Back")
            }
            Text(
                "Settings",
                style    = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── APPEARANCE ────────────────────────────────────────────────
            SettingsSection("Appearance")
            ToggleRow(Icons.Outlined.DarkMode, "Dark Theme", "Deep dark colour scheme", s.darkTheme, vm::toggleDarkTheme)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ToggleRow(Icons.Outlined.Palette, "Dynamic Color", "Use your wallpaper colours", s.dynamicColor, vm::toggleDynamicColor)
            }

            // ── PLAYBACK ──────────────────────────────────────────────────
            SettingsSection("Playback")
            ToggleRow(Icons.Outlined.Videocam, "Default Video Mode", "Show video for aD17 by default", s.defaultVideoMode, vm::toggleDefaultVideoMode)
            ToggleRow(Icons.Outlined.TravelExplore, "Scan on Launch", "Auto-scan storage when opening", s.scanOnLaunch, vm::toggleScanOnLaunch)

            // ── STORAGE ───────────────────────────────────────────────────
            SettingsSection("Storage")
            ActionRow(
                icon     = Icons.Rounded.Search,
                title    = "Scan Storage",
                subtitle = if (s.isScanning) "Scanning…" else (s.scanMsg ?: "Find music and aD17 files"),
                loading  = s.isScanning,
                onClick  = if (!s.isScanning) vm::scanStorage else null
            )

            // ── aD17 TOOLS ────────────────────────────────────────────────
            SettingsSection("aD17 Tools")
            ActionRow(
                icon     = Icons.Rounded.VideoFile,
                title    = "Convert Video to aD17",
                subtitle = "Convert any video file into encrypted aD17 format",
                onClick  = onNavigateToConverter,
                chevron  = true
            )

            // ── ABOUT ─────────────────────────────────────────────────────
            SettingsSection("About")
            InfoRow(Icons.Outlined.Info,        "Auralyx Player",       "Version 2.1.0")
            InfoRow(Icons.Outlined.AudioFile,   "Supported Formats",    "MP3 • MP4 • AAC • FLAC • OGG • aD17")
            InfoRow(Icons.Outlined.Code,        "Architecture",         "Jetpack Compose · Media3 · Hilt · Room")

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        title,
        style    = MaterialTheme.typography.labelMedium,
        color    = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp)
    )
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChanged: (Boolean) -> Unit
) {
    ListItem(
        headlineContent   = { Text(title, style = MaterialTheme.typography.titleSmall) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
        leadingContent    = {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Indigo.copy(0.12f)), Alignment.Center) {
                Icon(icon, null, tint = Indigo, modifier = Modifier.size(22.dp))
            }
        },
        trailingContent = { Switch(checked = checked, onCheckedChange = onChanged) }
    )
}

@Composable
private fun ActionRow(
    icon: ImageVector, title: String, subtitle: String,
    loading: Boolean = false, onClick: (() -> Unit)? = null, chevron: Boolean = false
) {
    ListItem(
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        headlineContent   = { Text(title, style = MaterialTheme.typography.titleSmall) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
        leadingContent    = {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Indigo.copy(0.12f)), Alignment.Center) {
                Icon(icon, null, tint = Indigo, modifier = Modifier.size(22.dp))
            }
        },
        trailingContent = {
            when {
                loading -> CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                chevron -> Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
            }
        }
    )
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, value: String) {
    ListItem(
        headlineContent   = { Text(title, style = MaterialTheme.typography.titleSmall) },
        supportingContent = { Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent    = {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh), Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            }
        }
    )
}
