/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.psippr.desktop.managers.ai.tts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.helpers.system.shell.Shell
import org.psippr.desktop.log.Log
import org.psippr.desktop.managers.ai.tts.base.TTSPlugin
import java.io.File

@Suppress("unused")
class TTSPluginSwift(
    private val context: IDesktopContext,
    private val scope: CoroutineScope = context.scope,
) : TTSPlugin {
    override val isPresent: Boolean = File("/opt/swift").exists()

    override fun talk(
        text: String,
        clearQueue: Boolean,
    ) {
        Log.i("talking: $text")
        scope.launch(Dispatchers.Default) {
            Shell.executeAndRead(
                "/opt/swift/bin/swift",
                "\"${text.replace("\"", " ")}\"",
            )
        }
    }
}
