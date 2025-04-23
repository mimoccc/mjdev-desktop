/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.ai.tts

import org.mjdev.desktop.log.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeechEngine
import nl.marc_apps.tts.TextToSpeechFactory
import org.mjdev.desktop.context.DesktopContext
import org.mjdev.desktop.interfaces.IDesktopContext

@Suppress("unused")
class TTSPluginAndroid(
    context: IDesktopContext,
) : TTSPluginMain(context, {
    TextToSpeechFactory(
        (context as? DesktopContext)?.androidContext!!,
        TextToSpeechEngine.SystemDefault
    ).createOrNull()
}) {
    // todo
    override val isPresent: Boolean = true

    override fun talk(text: String, clearQueue: Boolean) {
        Log.i("talking: $text")
        context.scope.launch(Dispatchers.Default) {
            textToSpeech.await()?.say(text, clearQueue)
        }
    }
}
