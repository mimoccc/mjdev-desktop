package eu.mjdev.desktop.helpers

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max

class KCEFHelper(
    private val scope: CoroutineScope?,
    val initialized: MutableState<Boolean> = mutableStateOf(false),
    val restartRequired: MutableState<Boolean> = mutableStateOf(false),
    val downloading: MutableState<Int> = mutableStateOf(0)
) {
    fun init() {
        if (!initialized.value && !restartRequired.value) {
            scope?.launch(Dispatchers.IO) {
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
    }

    fun dispose() {
        KCEF.disposeBlocking()
    }
}