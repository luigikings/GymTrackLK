package com.example.gymapplktrack.ui.theme

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode

@Composable
fun Modifier.gymGradientBackground(): Modifier {
    val colors = MaterialTheme.colorScheme
    val gradient = remember(colors) {
        Brush.verticalGradient(
            colors = listOf(
                colors.background,
                colors.surface,
                colors.background
            ),
            tileMode = TileMode.Clamp
        )
    }
    return this.background(gradient)
}

@Composable
fun Modifier.gymCardBackground(): Modifier {
    val colors = MaterialTheme.colorScheme
    val gradient = remember(colors) {
        Brush.linearGradient(
            colors = listOf(colors.surface, colors.surfaceVariant.copy(alpha = 0.9f)),
            tileMode = TileMode.Clamp
        )
    }
    return this.background(gradient)
}
