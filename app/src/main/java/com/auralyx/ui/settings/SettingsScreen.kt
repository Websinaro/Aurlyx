package com.auralyx.ui.settings

import android.os.Build

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*

import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // ───────────────── HEADER ─────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "Settings",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            // ───────────────── APPEARANCE ─────────────────
            SettingsSection("Appearance")

            ToggleRow(
                icon = Icons.Outlined.DarkMode,
                title = "Dark Theme",
                subtitle = "Deep dark colour scheme",
                checked = s.darkTheme,
                onChanged = vm::toggleDarkTheme
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ToggleRow(
                    icon = Icons.Outlined.Palette,
                    title = "Dynamic Color",
                    subtitle = "Use wallpaper colours",
                    checked = s.dynamicColor,
                    onChanged = vm::toggleDynamicColor
                )
            }

            // ───────────────── PLAYBACK ─────────────────
            SettingsSection("Playback")

            ToggleRow(
                icon = Icons.Outlined.Videocam,
                title = "Default Video Mode",
                subtitle = "Play aD17 videos automatically",
                checked = s.defaultVideoMode,
                onChanged = vm::toggleDefaultVideoMode
            )

            ToggleRow(
                icon = Icons.Outlined.TravelExplore,
                title = "Scan on Launch",
                subtitle = "Automatically scan storage",
                checked = s.scanOnLaunch,
                onChanged = vm::toggleScanOnLaunch
            )

            // ───────────────── STORAGE ─────────────────
            SettingsSection("Storage")

            ActionRow(
                icon = Icons.Rounded.Search,
                title = "Scan Storage",
                subtitle = if (s.isScanning) {
                    "Scanning media files..."
                } else {
                    s.scanMsg ?: "Find music and aD17 files"
                },
                loading = s.isScanning,
                onClick = if (!s.isScanning) vm::scanStorage else null
            )

            // ───────────────── aD17 TOOLS ─────────────────
            SettingsSection("aD17 Tools")

            ActionRow(
                icon = Icons.Rounded.VideoFile,
                title = "Convert Video to aD17",
                subtitle = "Encrypt and convert video files",
                chevron = true,
                onClick = onNavigateToConverter
            )

            // ───────────────── ABOUT ─────────────────
            SettingsSection("About")

            InfoRow(
                icon = Icons.Outlined.Info,
                title = "Auralyx Player",
                value = "Version 2.1.0"
            )

            InfoRow(
                icon = Icons.Outlined.AudioFile,
                title = "Supported Formats",
                value = "MP3 • AAC • FLAC • OGG • MP4 • aD17"
            )

            InfoRow(
                icon = Icons.Outlined.Code,
                title = "Architecture",
                value = "Compose • Media3 • Hilt • Room"
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String) {

    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 6.dp
        )
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {

        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
            },

            supportingContent = {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall
                )
            },

            leadingContent = {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Indigo.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Indigo,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },

            trailingContent = {

                Switch(
                    checked = checked,
                    onCheckedChange = onChanged
                )
            }
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    loading: Boolean = false,
    chevron: Boolean = false,
    onClick: (() -> Unit)? = null
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {

        ListItem(
            modifier = if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            },

            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
            },

            supportingContent = {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall
                )
            },

            leadingContent = {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Indigo.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Indigo,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },

            trailingContent = {

                when {

                    loading -> {

                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    }

                    chevron -> {

                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {

        ListItem(

            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
            },

            supportingContent = {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },

            leadingContent = {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        )
    }
}
