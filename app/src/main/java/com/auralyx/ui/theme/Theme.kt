package com.auralyx.ui.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val DarkColors = darkColorScheme(
    primary             = Indigo,
    onPrimary           = White100,
    primaryContainer    = Color(0xFF2D1F6E),
    onPrimaryContainer  = IndigoBright,
    secondary           = Rose,
    onSecondary         = White100,
    secondaryContainer  = Color(0xFF5C1A3A),
    onSecondaryContainer= Color(0xFFFFB3D0),
    tertiary            = Cyan,
    onTertiary          = Color(0xFF003540),
    tertiaryContainer   = Color(0xFF004D5C),
    onTertiaryContainer = Color(0xFFA0F0FF),
    background          = Violet900,
    onBackground        = White90,
    surface             = Violet800,
    onSurface           = White90,
    surfaceVariant      = Violet700,
    onSurfaceVariant    = White70,
    surfaceContainerLowest = Violet900,
    surfaceContainerLow    = Violet800,
    surfaceContainer       = Violet750,
    surfaceContainerHigh   = Violet700,
    surfaceContainerHighest= Violet600,
    outline             = White20,
    outlineVariant      = White10,
    scrim               = Color(0xCC000000),
    error               = Color(0xFFCF6679),
    onError             = White100,
    inverseSurface      = Color(0xFFE8E3FF),
    inverseOnSurface    = LightText,
    inversePrimary      = IndigoSoft
)

val LightColors = lightColorScheme(
    primary             = IndigoSoft,
    onPrimary           = White100,
    primaryContainer    = Light200,
    onPrimaryContainer  = Color(0xFF2D0C8E),
    secondary           = Rose,
    onSecondary         = White100,
    background          = Light50,
    onBackground        = LightText,
    surface             = White100,
    onSurface           = LightText,
    surfaceVariant      = Light100,
    onSurfaceVariant    = LightMuted,
    outline             = Color(0xFFBBB3E0),
    outlineVariant      = Color(0xFFDDD6FE)
)

@Composable
fun AuralyxTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else      -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, typography = AuralyxTypography, content = content)
}
