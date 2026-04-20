package com.solotodo.designsystem.theme

import android.app.Activity
import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.solotodo.designsystem.SoloTokens

/**
 * Root theme for Solo ToDo.
 * Wraps MaterialTheme with a dark color scheme pulled from [SoloTokens.Colors].
 * The app is dark-only by design — no light mode exists in the prototype.
 */
@Composable
fun SoloTodoTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = darkColorScheme(
        primary = SoloTokens.Colors.Stroke,
        onPrimary = SoloTokens.Colors.BgVoid,
        primaryContainer = SoloTokens.Colors.BgPanel,
        onPrimaryContainer = SoloTokens.Colors.Text,
        secondary = SoloTokens.Colors.Glow,
        onSecondary = SoloTokens.Colors.BgVoid,
        tertiary = SoloTokens.Colors.AccentShadow,
        onTertiary = SoloTokens.Colors.BgVoid,
        background = SoloTokens.Colors.BgVoid,
        onBackground = SoloTokens.Colors.Text,
        surface = SoloTokens.Colors.BgPanel,
        onSurface = SoloTokens.Colors.Text,
        surfaceVariant = SoloTokens.Colors.BgPanelRaised,
        onSurfaceVariant = SoloTokens.Colors.TextMuted,
        error = SoloTokens.Colors.Danger,
        onError = SoloTokens.Colors.Text,
        outline = SoloTokens.Colors.StrokeDim,
    )

    val view: View = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SoloTypography,
        content = content,
    )
}
