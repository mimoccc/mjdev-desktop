/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.artificialintelligence.tts

import eu.mjdev.desktop.helpers.system.Shell
import eu.mjdev.desktop.managers.artificialintelligence.tts.base.TTSPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("unused")
class TTSPluginSwift(
    val scope: CoroutineScope
) : TTSPlugin {
    override fun talk(text: String, clearQueue: Boolean) {
        println("talking: $text")
        scope.launch(Dispatchers.IO) {
            Shell.executeAndRead(
                "/opt/swift/bin/swift",
                "\"${text.replace("\"", " ")}\""
            )
        }
    }
}