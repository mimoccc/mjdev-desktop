/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.artificialintelligence

import eu.mjdev.desktop.managers.artificialintelligence.base.AIPlugin
import eu.mjdev.desktop.managers.artificialintelligence.plugins.AiPluginNull
import eu.mjdev.desktop.managers.artificialintelligence.stt.STTPluginNull
import eu.mjdev.desktop.managers.artificialintelligence.stt.base.STTPlugin
import eu.mjdev.desktop.managers.artificialintelligence.tts.TTSPluginMain
import eu.mjdev.desktop.managers.artificialintelligence.tts.base.TTSPlugin
import eu.mjdev.desktop.provider.DesktopProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("MemberVisibilityCanBePrivate", "unused")
class AIManager(
    val api: DesktopProvider,
    var pluginAI: AIPlugin,
    var pluginSTT: STTPlugin,
    var pluginTTS: TTSPlugin
) {
    val scope = api.scope
    val isAvailable: () -> Boolean = { pluginAI !is AiPluginNull }

    fun ask(
        question: String,
        block: AIManager.(question: String, result: String) -> Unit
    ) = scope.launch(Dispatchers.IO) {
        pluginAI.ask(question).also { result ->
            block.invoke(this@AIManager, question, result)
        }
    }

    fun talk(text: String, clearQueue: Boolean = false) =
        pluginTTS.talk(text, clearQueue)

    companion object {
        fun aiManager(
            api: DesktopProvider,
            scope: CoroutineScope = api.scope,
            ai: AIPlugin = AiPluginNull(scope),
            stt: STTPlugin = STTPluginNull(scope),
            tts: TTSPlugin = TTSPluginMain(scope)
        ) = AIManager(api, ai, stt, tts)
    }
}