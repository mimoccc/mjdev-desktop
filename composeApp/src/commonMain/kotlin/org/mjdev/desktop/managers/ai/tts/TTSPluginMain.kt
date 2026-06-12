/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.ai.tts

import kotlinx.coroutines.Deferred
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.managers.ai.tts.base.TTSPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeechInstance
import org.mjdev.desktop.context.IDesktopContext

open class TTSPluginMain(
    val context: IDesktopContext,
    val instantiate: suspend IDesktopContext.() -> TextToSpeechInstance? = { null }
) : TTSPlugin {
    // todo
    override val isPresent: Boolean = true

    val textToSpeech: Deferred<TextToSpeechInstance?> = context.scope.async {
        runCatching { instantiate(context) }.getOrNull()
    }

    override fun talk(text: String, clearQueue: Boolean) {
        Log.i("talking: $text")
        context.scope.launch(Dispatchers.Default) {
            textToSpeech.await()?.say(text, clearQueue)
        }
    }
}
