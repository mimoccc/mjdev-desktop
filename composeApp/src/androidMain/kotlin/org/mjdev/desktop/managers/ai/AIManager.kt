/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.ai

import org.mjdev.desktop.managers.ai.plugins.AiPluginEmpty
import org.mjdev.desktop.managers.ai.stt.STTPluginEmpty
import org.mjdev.desktop.managers.ai.stt.base.STTPlugin
import org.mjdev.desktop.managers.ai.tts.base.TTSPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.desktop.interfaces.IDesktopContext
import org.mjdev.desktop.managers.ai.actions.ActionCalculator
import org.mjdev.desktop.managers.ai.actions.ActionsProvider
import org.mjdev.desktop.managers.ai.actions.base.ActionException.ActionNone
import org.mjdev.desktop.managers.ai.plugins.AiPluginGemini
import org.mjdev.desktop.managers.ai.tts.TTSPluginMain

@Suppress("MemberVisibilityCanBePrivate", "unused", "FunctionName")
class AIManager(
    val context: IDesktopContext,
    var pluginAI: AIPlugin,
    var pluginSTT: STTPlugin,
    var pluginTTS: TTSPlugin,
    var actions: ActionsProvider
) : IAiManager {

    override val isAvailable: () -> Boolean = { pluginAI !is AiPluginEmpty }

    override fun ask(
        question: String,
        block: (question: String, result: String) -> Unit
    ) {
        context.scope.launch(Dispatchers.Default) {
            val actResult = actions.tryAction(question)
            if (actResult is ActionNone) {
                pluginAI.ask(question).also { result ->
                    block.invoke(question, result)
                }
            } else {
                actResult.message?.also { message ->
                    block.invoke(question, message)
                }
            }
        }
    }

    override fun talk(
        text: String,
        clearQueue: Boolean
    ) = pluginTTS.talk(text, clearQueue)

    companion object {
        fun AiManager(
            context: IDesktopContext,
            ai: AIPlugin = AiPluginGemini(context),
            stt: STTPlugin = STTPluginEmpty(context),
            tts: TTSPlugin = TTSPluginMain(context),
            actions: ActionsProvider = ActionsProvider(context) {
                ActionCalculator // todo infix fnc
            }
        ) = AIManager(context, ai, stt, tts, actions)
    }
}