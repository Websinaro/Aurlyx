package com.auralyx.ui.converter

import android.net.Uri
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Movie
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auralyx.ui.theme.Indigo
import com.auralyx.ui.theme.Rose

@Composable
fun ConverterScreen(onBack: () -> Unit, vm: ConverterViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsState()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { vm.enqueue(it) }
    }

    Column(Modifier.fillMaxSize()) {
        // ── Header ────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().statusBarsPadding()
                .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBackIosNew, "Back")
            }
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Text("Convert to aD17", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Encrypt videos as aD17 files", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Info card ─────────────────────────────────────────────────
            InfoBanner()

            // ── Quality selector ──────────────────────────────────────────
            QualitySelector(
                selected = state.selectedQuality,
                onSelect = vm::selectQuality
            )

            // ── Pick file button ──────────────────────────────────────────
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Button(
                    onClick  = { picker.launch(arrayOf("video/*")) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Indigo)
                ) {
                    Icon(Icons.Rounded.VideoFile, null)
                    Spacer(Modifier.width(10.dp))
                    Text("Select Video", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }

            // ── Queue ─────────────────────────────────────────────────────
            if (state.queue.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Text("Conversion Queue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    val hasFailed = state.queue.any { it.status == ConversionStatus.FAILED }
                    if (hasFailed) {
                        TextButton(onClick = vm::retryFailed) {
                            Icon(Icons.Rounded.Refresh, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Retry Failed")
                        }
                    }
                }

                state.queue.forEach { job ->
                    JobRow(
                        job      = job,
                        onCancel = { vm.cancel(job.id) },
                        onRemove = { vm.remove(job.id) }
                    )
                }
            } else {
                EmptyQueueHint()
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun InfoBanner() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Indigo.copy(0.10f))
            .padding(14.dp),
        Arrangement.spacedBy(12.dp),
        Alignment.Top
    ) {
        Icon(Icons.Rounded.Info, null, tint = Indigo, modifier = Modifier.size(20.dp).padding(top = 1.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("About aD17 Conversion", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Indigo)
            Text(
                "Videos are re-encoded and saved as encrypted .aD17 files in your Downloads folder. " +
                "The process preserves audio and video quality based on your chosen preset. " +
                "Conversion runs in the background and you can add multiple files to the queue.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QualitySelector(selected: ConvertQuality, onSelect: (ConvertQuality) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text("Output Quality", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            ConvertQuality.values().forEach { q ->
                val chosen = q == selected
                Box(
                    Modifier.weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (chosen) Indigo else MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { onSelect(q) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            when (q) {
                                ConvertQuality.STANDARD -> Icons.Rounded.HdOutlined
                                ConvertQuality.HIGH     -> Icons.Rounded.FourK
                                ConvertQuality.ULTRA    -> Icons.Rounded.AutoAwesome
                            },
                            null,
                            tint = if (chosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            q.label,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal,
                            color      = if (chosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            when (q) {
                                ConvertQuality.STANDARD -> "720p / 192kbps"
                                ConvertQuality.HIGH     -> "1080p / 256kbps"
                                ConvertQuality.ULTRA    -> "1440p / 320kbps"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (chosen) Color.White.copy(0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.65f)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun JobRow(job: ConversionJob, onCancel: () -> Unit, onRemove: () -> Unit) {
    val bg = when (job.status) {
        ConversionStatus.DONE       -> MaterialTheme.colorScheme.surfaceContainer
        ConversionStatus.FAILED     -> MaterialTheme.colorScheme.errorContainer.copy(0.3f)
        ConversionStatus.CANCELLED  -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(0.5f)
        else                        -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp)).background(bg).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Status icon
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(statusColor(job.status).copy(0.16f)), Alignment.Center) {
                when (job.status) {
                    ConversionStatus.CONVERTING -> {
                        val inf = rememberInfiniteTransition(label = "conv${job.id}")
                        val rot by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "r")
                        CircularProgressIndicator(Modifier.size(22.dp).rotate(rot), color = Indigo, strokeWidth = 2.5.dp, trackColor = Indigo.copy(0.2f))
                    }
                    ConversionStatus.DONE       -> Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(22.dp))
                    ConversionStatus.FAILED     -> Icon(Icons.Rounded.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                    ConversionStatus.CANCELLED  -> Icon(Icons.Rounded.Cancel, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                    ConversionStatus.QUEUED     -> Icon(Icons.Rounded.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                }
            }

            Column(Modifier.weight(1f)) {
                Text(job.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    when (job.status) {
                        ConversionStatus.QUEUED     -> "Queued • ${job.quality.label}"
                        ConversionStatus.CONVERTING -> "Converting… ${(job.progress * 100).toInt()}%"
                        ConversionStatus.DONE       -> "Saved to Downloads ✓"
                        ConversionStatus.FAILED     -> "Failed: ${job.error ?: "unknown error"}"
                        ConversionStatus.CANCELLED  -> "Cancelled"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (job.status) {
                        ConversionStatus.DONE   -> Color(0xFF22C55E)
                        ConversionStatus.FAILED -> MaterialTheme.colorScheme.error
                        else                    -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }

            // Action button
            when (job.status) {
                ConversionStatus.QUEUED,
                ConversionStatus.CONVERTING -> IconButton(onClick = onCancel, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Close, "Cancel", Modifier.size(18.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, "Remove", Modifier.size(18.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Progress bar (only while converting)
        if (job.status == ConversionStatus.CONVERTING) {
            val prog by animateFloatAsState(job.progress, tween(400), label = "jobProg")
            LinearProgressIndicator(
                progress           = { prog },
                modifier           = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color              = Indigo,
                trackColor         = Indigo.copy(0.18f),
                strokeCap          = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun EmptyQueueHint() {
    Column(
        Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val inf = rememberInfiniteTransition(label = "hint")
        val alpha by inf.animateFloat(0.35f, 0.75f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ha")
        Icon(Icons.Rounded.VideoFile, null, Modifier.size(64.dp).alpha(alpha), Indigo.copy(0.55f))
        Text("No conversions yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text("Select a video file above to start converting it to the aD17 format.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun statusColor(s: ConversionStatus) = when (s) {
    ConversionStatus.DONE      -> Color(0xFF22C55E)
    ConversionStatus.FAILED    -> MaterialTheme.colorScheme.error
    ConversionStatus.CONVERTING-> Indigo
    else                       -> MaterialTheme.colorScheme.onSurfaceVariant
}
