package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF043825),
    primaryContainer = Color(0xFF20483A),
    onPrimaryContainer = Color(0xFFC7EBD7),
    secondary = DarkSecondary,
    onSecondary = Color(0xFF452C14),
    secondaryContainer = Color(0xFF62462A),
    onSecondaryContainer = Color(0xFFFFDDB8),
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    outline = Color(0xFF4E5E56)
)

private val LightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = Color.White,
    primaryContainer = SagePrimaryContainer,
    onPrimaryContainer = OnSagePrimaryContainer,
    secondary = SandSecondary,
    onSecondary = Color.White,
    secondaryContainer = SandSecondaryContainer,
    onSecondaryContainer = OnSandSecondaryContainer,
    tertiary = SlateTertiary,
    background = SoftBackground,
    surface = SoftSurface,
    surfaceVariant = SoftSurfaceVariant,
    outline = SoftOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep soothing custom palette by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
