package com.auralyx.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PermissionRationale(onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.FolderOpen, null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        Text("Storage Access Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("Auralyx needs access to your storage to find and play music and .aD17 video files.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onGrant, modifier = Modifier.fillMaxWidth()) {
            Text("Grant Permission")
        }
    }
}
