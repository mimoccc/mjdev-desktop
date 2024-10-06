package eu.mjdev.desktop.managers

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.datlag.kcef.KCEF
import eu.mjdev.desktop.provider.DesktopProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max

class KCEFHelper(
    private val api: DesktopProvider,
    private val scope: CoroutineScope = api.scope,
    val initialized: MutableState<Boolean> = mutableStateOf(false),
    val restartRequired: MutableState<Boolean> = mutableStateOf(false),
    val downloading: MutableState<Int> = mutableStateOf(0)
) {
    fun init() = scope.launch(Dispatchers.IO) {
        if (!initialized.value && !restartRequired.value) {
            KCEF.init(builder = {
                installDir(File("/tmp/kcef-bundle"))
                progress {
                    onDownloading {
                        downloading.value = max(it, 0F).toInt()
                    }
                    onInitialized {
                        initialized.value = true
                    }
                }
                settings {
                    cachePath = File("cache").absolutePath
                }
            }, onError = { err ->
                err?.printStackTrace()
            }, onRestartRequired = {
                restartRequired.value = true
            })
        }
    }

    fun dispose() {
        KCEF.disposeBlocking()
    }
}
