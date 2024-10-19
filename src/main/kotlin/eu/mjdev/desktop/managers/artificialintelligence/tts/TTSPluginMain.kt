/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.artificialintelligence.tts

import eu.mjdev.desktop.log.Log
import eu.mjdev.desktop.managers.artificialintelligence.tts.base.TTSPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeechFactory
import nl.marc_apps.tts.experimental.ExperimentalDesktopTarget

@OptIn(ExperimentalDesktopTarget::class)
class TTSPluginMain(
    val scope: CoroutineScope
) : TTSPlugin {
    private val textToSpeech = scope.async {
        TextToSpeechFactory().createOrNull()
    }

    override fun talk(text: String, clearQueue: Boolean) {
        Log.i("talking: $text")
        scope.launch(Dispatchers.IO) {
            textToSpeech.await()?.say(text, clearQueue)
        }
    }
}
