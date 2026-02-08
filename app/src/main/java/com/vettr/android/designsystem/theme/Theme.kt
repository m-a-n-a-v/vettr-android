package com.vettr.android.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@Preview(showBackground = true)
@Composable
fun VettrThemePreview() {
    VettrTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "VETTR Theme Preview",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Title Large",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Body Large - This is sample body text",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Card(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Sample Card with VettrCardBackground",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Primary Color Text",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
