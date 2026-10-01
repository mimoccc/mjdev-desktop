package org.mjdev.desktop.components.blur

import androidx.compose.runtime.compositionLocalOf

/**
 * Screen bounds of the current window. The desktop window provider keeps it live while the window
 * moves or resizes; platforms that cannot tell keep the zero default, which disables glass.
 */
val LocalWindowBounds = compositionLocalOf { WindowBounds() }
