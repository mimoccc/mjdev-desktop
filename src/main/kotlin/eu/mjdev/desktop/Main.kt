package eu.mjdev.desktop

import eu.mjdev.desktop.gtk.application
import eu.mjdev.desktop.gtk.components.ApplicationWindow
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import eu.mjdev.desktop.components.main.MainWindow
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.DesktopProvider.Companion.rememberDesktopProvider

//fun main(
//    args: Array<String>
//) = application(
//    args = args.toList(),
//    exitProcessOnExit = true
//) {
//    // net devices  : ls /sys/class/net
//    // wifi list    : nmcli -t -f ALL dev wifi
//    // wave mon     : apt install wavemon
//    // connect gui  : nmtui
//    // eth settings : iwconfig
//    MainWindow()
//    DisposableEffect(Unit) {
//        println("App started with args: ${args.toList()}")
//        Shell {
//            // todo
////            autoStartApps()
//        }
//        onDispose {
//            println("App ended.")
//        }
//    }
//}

fun main() {
    val args: Array<String> = emptyArray()
    application("eu.mjdev.Desktop", args) {
        ApplicationWindow(
            application,
            "",
            onClose = ::exitApplication
        ) {
            val api = rememberDesktopProvider()
            println("Started with args: ${args.toList()}")
            MaterialTheme {
                CompositionLocalProvider(LocalDesktop provides api) {
                    MainWindow()
                }
            }
        }
    }
}