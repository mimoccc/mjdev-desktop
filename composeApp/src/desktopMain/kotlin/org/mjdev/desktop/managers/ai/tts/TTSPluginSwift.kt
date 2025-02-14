/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.ai.tts

import org.mjdev.desktop.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.desktop.helpers.system.shell.Shell
import org.mjdev.desktop.interfaces.IDesktopContext
import org.mjdev.desktop.managers.ai.tts.base.TTSPlugin

@Suppress("unused")
class TTSPluginSwift(
    private val context: IDesktopContext,
    private val scope: CoroutineScope = context.scope
) : TTSPlugin {
    override fun talk(text: String, clearQueue: Boolean) {
        Log.i("talking: $text")
        scope.launch(Dispatchers.Default) {
            Shell.executeAndRead(
                "/opt/swift/bin/swift",
                "\"${text.replace("\"", " ")}\""
            )
        }
    }
}
