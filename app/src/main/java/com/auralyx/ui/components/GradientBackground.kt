package com.auralyx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.auralyx.ui.theme.Indigo
import com.auralyx.ui.theme.Rose

@Composable
fun GradientBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.radialGradient(
                0.0f to MaterialTheme.colorScheme.background,
                0.6f to MaterialTheme.colorScheme.background,
                1.0f to Indigo.copy(alpha = 0.06f)
            )
        ),
        content = content
    )
}
