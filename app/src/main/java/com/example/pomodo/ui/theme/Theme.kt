package com.example.pomodo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    background = DarkBackgroundSurface,
    onBackground = DarkOnBackgroundSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    error = DarkError,
    onError = DarkOnError,
    secondary = DarkRed,
    onSecondary = Color.White,
    tertiary = DarkPrimary,
    onTertiary = Color.White,
    surfaceVariant = DarkRed,
    onSurfaceVariant = Color.White,
    outline = LightBackground
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    background = LightBackgroundSurface,
    onBackground = LightOnBackgroundSurface,
    surface = LightBackground,
    onSurface = DarkBackground,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    error = LightError,
    onError = LightOnError,
    secondary = PrimaryRed,
    onSecondary = Color.White,
    tertiary = DarkRed,
    onTertiary = Color.White,
    surfaceVariant = PrimaryRed,
    onSurfaceVariant = DarkBackground, // Ajustado para DarkBackground para melhor legibilidade
    outline = DarkBackground
)

@Composable
fun PomodoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
