package eu.mjdev.desktop.windows.blur

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.WindowScope
import eu.mjdev.desktop.windows.blur.base.StubWindowBlurManager
import eu.mjdev.desktop.windows.blur.linux.LinuxWindowBlurManager
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.Window

@Suppress("unused")
interface WindowBlurManager {
    var blurEnabled: Boolean

    companion object {
        private fun WindowBlurManager(
            window: Window,
            blurEnabled: Boolean = false,
        ) = when (hostOs) {
            // todo other platforms
            OS.Linux -> LinuxWindowBlurManager(window, blurEnabled)
            else -> StubWindowBlurManager(blurEnabled)
        }

        @Composable
        fun WindowScope.windowBlur(
            blurEnabled: Boolean = false,
        ) {
            val manager = remember { WindowBlurManager(window, blurEnabled) }
            LaunchedEffect(blurEnabled) {
                manager.blurEnabled = blurEnabled
            }
        }
    }
}
