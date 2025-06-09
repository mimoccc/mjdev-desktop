package org.mjdev.desktop.context

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

actual val LocalDesktopContext: ProvidableCompositionLocal<IDesktopContext> = compositionLocalOf {
    DesktopContext()
}