/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.ai.tts

import org.mjdev.desktop.log.Log
import org.mjdev.desktop.managers.ai.tts.base.TTSPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeechFactory
import nl.marc_apps.tts.experimental.ExperimentalDesktopTarget
import org.mjdev.desktop.interfaces.IDesktopContext

@Suppress("unused")
class TTSPluginMain(
    val context: IDesktopContext
) : TTSPlugin {
    @OptIn(ExperimentalDesktopTarget::class)
    private val textToSpeech = context.scope.async {
        TextToSpeechFactory().createOrNull()
    }

    override fun talk(text: String, clearQueue: Boolean) {
        Log.i("talking: $text")
        context.scope.launch(Dispatchers.Default) {
            textToSpeech.await()?.say(text, clearQueue)
        }
    }
}
