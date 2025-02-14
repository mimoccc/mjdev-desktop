package org.mjdev.desktop.windows.blur

//import org.mjdev.desktop.windows.blur.linux.LinuxWindowBlurManager

//@Suppress("unused")
//interface WindowBlurManager {
//    var blurEnabled: Boolean

//    companion object {
//        private fun WindowBlurManager(
//            window: Window,
//            blurEnabled: Boolean = false,
//        ) = when (hostOs) {
//            // todo other platforms
////            OS.Linux -> LinuxWindowBlurManager(window, blurEnabled)
//            else -> StubWindowBlurManager(blurEnabled)
//        }

//        @Composable
//        fun WindowScope.windowBlur(
//            blurEnabled: Boolean = false,
//        ) {
//            val manager = remember { org.mjdev.desktop.windows.blur.WindowBlurManager(window, blurEnabled) }
//            LaunchedEffect(blurEnabled) {
//                manager.blurEnabled = blurEnabled
//            }
//        }
//    }
//}
