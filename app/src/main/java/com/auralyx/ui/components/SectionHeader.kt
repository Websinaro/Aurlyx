package com.auralyx.ui.components
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment; import androidx.compose.ui.Modifier; import androidx.compose.ui.unit.dp
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, actionLabel: String? = null, onAction: (()->Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(title, style=MaterialTheme.typography.headlineSmall)
        if (actionLabel != null && onAction != null) {
            TextButton(onClick=onAction) { Text(actionLabel, style=MaterialTheme.typography.labelMedium, color=MaterialTheme.colorScheme.primary) }
        }
    }
}
