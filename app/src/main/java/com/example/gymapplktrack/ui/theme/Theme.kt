package com.example.gymapplktrack.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

private val LightColors = lightColorScheme(
    primary = InkBlack,
    onPrimary = FrostWhite,
    primaryContainer = InkBlack,
    onPrimaryContainer = FrostWhite,
    secondary = BattleScarlet,
    onSecondary = FrostWhite,
    secondaryContainer = Gunmetal,
    onSecondaryContainer = FrostWhite,
    tertiary = ElectricAccent,
    onTertiary = InkBlack,
    background = FrostWhite,
    surface = GhostWhite,
    surfaceVariant = GhostWhite,
    onSurface = InkBlack,
    onSurfaceVariant = InkBlack.copy(alpha = 0.7f),
    outline = OutlineGray
)

private val DarkColors = darkColorScheme(
    primary = FrostWhite,
    onPrimary = InkBlack,
    primaryContainer = BattleScarlet,
    onPrimaryContainer = FrostWhite,
    secondary = BattleScarlet,
    onSecondary = FrostWhite,
    secondaryContainer = ShadowTint,
    onSecondaryContainer = FrostWhite,
    tertiary = ElectricAccent,
    onTertiary = InkBlack,
    background = InkBlack,
    surface = Carbon,
    surfaceVariant = ForgedSteel,
    onSurface = GhostWhite,
    onSurfaceVariant = GhostWhite.copy(alpha = 0.7f),
    outline = OutlineGray
)

@Composable
fun GymTrackTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !useDarkTheme
            controller.isAppearanceLightNavigationBars = !useDarkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
