package com.vettr.android.designsystem.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val VettrDarkColorScheme = darkColorScheme(
    primary = VettrAccent,
    onPrimary = VettrOnPrimary,
    primaryContainer = VettrAccent.copy(alpha = 0.12f),
    onPrimaryContainer = VettrAccent,
    secondary = VettrAccent,
    onSecondary = VettrOnPrimary,
    secondaryContainer = VettrAccent.copy(alpha = 0.12f),
    onSecondaryContainer = VettrAccent,
    tertiary = VettrWarning,
    onTertiary = VettrOnPrimary,
    error = VettrError,
    onError = VettrOnPrimary,
    errorContainer = VettrError.copy(alpha = 0.12f),
    onErrorContainer = VettrError,
    background = VettrNavy,
    onBackground = VettrOnSurface,
    surface = VettrSurface,
    onSurface = VettrOnSurface,
    surfaceVariant = VettrSurfaceVariant,
    onSurfaceVariant = VettrOnSurfaceVariant,
    surfaceContainerLowest = VettrNavy,
    surfaceContainerLow = VettrSurfaceContainer,
    surfaceContainer = VettrSurfaceContainer,
    surfaceContainerHigh = VettrSurfaceVariant,
    surfaceContainerHighest = VettrCardBackground,
    outline = VettrOutline,
    outlineVariant = VettrOutlineVariant,
    inverseSurface = VettrTextPrimary,
    inverseOnSurface = VettrNavy,
    inversePrimary = VettrAccent,
    scrim = VettrNavy.copy(alpha = 0.6f),
)

@Composable
fun VettrTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = VettrDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VettrTypography,
        content = content,
    )
}
