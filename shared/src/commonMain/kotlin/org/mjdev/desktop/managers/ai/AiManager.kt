/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.managers.ai.actions.ActionCalculator
import org.mjdev.desktop.managers.ai.actions.ActionCurrentTime
import org.mjdev.desktop.managers.ai.actions.base.ActionException.ActionNone
import org.mjdev.desktop.managers.ai.actions.base.ActionsProvider
import org.mjdev.desktop.managers.ai.plugins.AiPluginEmpty
import org.mjdev.desktop.managers.ai.plugins.base.AIPlugin
import org.mjdev.desktop.managers.ai.stt.STTPluginEmpty
import org.mjdev.desktop.managers.ai.stt.base.STTPlugin
import org.mjdev.desktop.managers.ai.tts.TTSPluginEmpty
import org.mjdev.desktop.managers.ai.tts.base.TTSPlugin

@Suppress("MemberVisibilityCanBePrivate", "unused")
class AiManager(
    val context: IDesktopContext,
    var pluginAI: AIPlugin = AiPluginEmpty(context),
    var pluginSTT: STTPlugin = STTPluginEmpty(context),
    var pluginTTS: TTSPlugin = TTSPluginEmpty(context),
    var actions: ActionsProvider =
        ActionsProvider(context) {
            ActionCalculator // todo infix fnc
            ActionCurrentTime
        },
) : IAiManager {
    override val isAvailable: () -> Boolean = { pluginAI !is AiPluginEmpty }

    override fun ask(
        question: String,
        block: IAiManager.(question: String, result: String) -> Unit,
    ) {
        context.scope.launch(Dispatchers.Default) {
            val actResult = actions.tryAction(question)
            if (actResult is ActionNone) {
                pluginAI.ask(question).also { result ->
                    block.invoke(this@AiManager, question, result)
                }
            } else {
                actResult.message?.also { message ->
                    block.invoke(this@AiManager, question, message)
                }
            }
        }
    }

    override fun say(
        text: String,
        clearQueue: Boolean,
    ) = pluginTTS.talk(text, clearQueue)
}
