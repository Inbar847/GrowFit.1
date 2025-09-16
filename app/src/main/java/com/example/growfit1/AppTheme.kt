package com.example.growfit1

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ירוק עדין (Light)
private val LightColors = lightColorScheme(
    primary = Color(0xFF66BB6A),          // ירוק רך (primary)
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5D6A7), // רקע ירקרק ל-AppBar/כפתורים מוגבהים
    onPrimaryContainer = Color(0xFF10391A),
    secondary = Color(0xFF80CBC4)         // ירקרק-מנטה עדין
)

// גרסה כהה (Dark)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003914),
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color(0xFFA8E6B0),
    secondary = Color(0xFF4DB6AC)
)

@Composable
fun GrowFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
