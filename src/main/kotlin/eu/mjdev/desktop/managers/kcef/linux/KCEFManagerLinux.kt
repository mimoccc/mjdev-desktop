/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.kcef.linux

import dev.datlag.kcef.KCEF
import eu.mjdev.desktop.managers.kcef.base.KCEFManagerStub
import eu.mjdev.desktop.provider.DesktopProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max

class KCEFManagerLinux(
    api: DesktopProvider
) : KCEFManagerStub(api) {
    private val scope: CoroutineScope get() = api.scope

    override fun init() {
        scope.launch(Dispatchers.IO) {
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
    }

    override fun dispose() {
        KCEF.disposeBlocking()
    }
}