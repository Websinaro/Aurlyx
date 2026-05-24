package com.auralyx.ui.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary             = PurpleAccent,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFF3B1F6B),
    onPrimaryContainer  = PurpleLight,
    secondary           = PinkAccent,
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFF5C1A3A),
    tertiary            = CyanAccent,
    onTertiary          = Color(0xFF003340),
    background          = DeepBlack,
    onBackground        = OnDark,
    surface             = SurfaceDark,
    onSurface           = OnDark,
    surfaceVariant      = CardDark,
    onSurfaceVariant    = OnDarkMuted,
    surfaceContainerLow = SurfaceDark,
    surfaceContainer    = CardDark,
    surfaceContainerHigh = CardDarkElevated,
    outline             = OnDarkSubtle,
    outlineVariant      = Color(0xFF2A2A45),
    error               = Color(0xFFCF6679),
    scrim               = Color.Black.copy(alpha = 0.6f)
)

private val LightColorScheme = lightColorScheme(
    primary             = LightAccent,
    onPrimary           = Color.White,
    primaryContainer    = LightAccentSoft,
    onPrimaryContainer  = Color(0xFF3B0F8C),
    secondary           = PinkAccent,
    onSecondary         = Color.White,
    tertiary            = CyanAccent,
    background          = LightBg,
    onBackground        = OnLight,
    surface             = LightSurface,
    onSurface           = OnLight,
    surfaceVariant      = LightCard,
    onSurfaceVariant    = OnLightMuted,
    outline             = Color(0xFFBBBBDD),
    outlineVariant      = Color(0xFFDDDDF8)
)

@Composable
fun AuralyxTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AuralyxTypography,
        content     = content
    )
}
