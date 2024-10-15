package eu.mjdev.desktop

import androidx.compose.runtime.DisposableEffect
import eu.mjdev.desktop.components.main.MainWindow
import eu.mjdev.desktop.helpers.application.application
import eu.mjdev.desktop.helpers.system.Shell

fun main(
    args: Array<String>
) = application(
    args = args.toList(),
    exitProcessOnExit = true
) {
    // net devices  : ls /sys/class/net
    // connect gui  : nmtui
    // eth settings : iwconfig
    MainWindow()
    DisposableEffect(Unit) {
        println("App started with args: ${args.toList()}")
        Shell {
            // todo
//            autoStartApps()
        }
        onDispose {
            println("App ended.")
        }
    }
}
