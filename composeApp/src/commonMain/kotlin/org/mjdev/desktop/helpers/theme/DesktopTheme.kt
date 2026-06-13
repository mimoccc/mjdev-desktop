package org.mjdev.desktop.helpers.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// todo palette
@Composable
fun DesktopTheme(content: @Composable () -> Unit) =
    MaterialTheme(
        colors =
            darkColors(
                primary = Color.Transparent,
                secondary = Color.Transparent,
                background = Color.Transparent,
                onBackground = Color.Transparent,
                onPrimary = Color.Transparent,
                onSurface = Color.Transparent,
                onError = Color.Transparent,
                onSecondary = Color.Transparent,
                error = Color.Transparent,
                primaryVariant = Color.Transparent,
                secondaryVariant = Color.Transparent,
                surface = Color.Transparent,
            ),
        content = content,
    )
