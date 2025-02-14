package org.mjdev.desktop.context

import androidx.compose.runtime.ProvidableCompositionLocal
import org.mjdev.desktop.interfaces.IDesktopContext

expect val LocalDesktopContext: ProvidableCompositionLocal<IDesktopContext>
