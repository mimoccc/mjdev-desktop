package org.mjdev.desktop.context

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.mjdev.desktop.interfaces.IDesktopContext

actual val LocalDesktopContext: ProvidableCompositionLocal<IDesktopContext> = compositionLocalOf {
    DesktopContext()
}