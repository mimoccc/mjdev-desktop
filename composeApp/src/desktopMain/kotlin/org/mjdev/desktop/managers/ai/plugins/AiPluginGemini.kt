/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.ai.plugins

import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.content
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.mjdev.desktop.interfaces.IDesktopContext
import org.mjdev.desktop.managers.ai.AIPlugin

/*
* Gemini key : https://makersuite.google.com/app/apikey
* */
class AiPluginGemini(
    private val context: IDesktopContext,
    private val aiModel: Model = Model.Gemini1_5Pro,
    private val scope: CoroutineScope = context.scope
) : AIPlugin {
    private val key = context.keysManager.loadKey("gemini")
    private val generativeModel: GenerativeModel? by lazy {
        if (key.isNotEmpty()) {
            GenerativeModel(
                modelName = aiModel.model,
                apiKey = key
            )
        } else null
    }

    override suspend fun ask(question: String): String = scope.async {
        var error: Throwable? = null
        runCatching {
            if (key.isEmpty()) {
                throw (Exception("Error: No gemini api key provided, pleas read manual and provide Your api key."))
            } else {
                generativeModel?.generateContent(
                    content {
                        text(question)
                    }
                )?.text
            }
        }.onFailure { e ->
            error = e
        }.getOrNull() ?: error?.message ?: ""
    }.await()

    @Suppress("EnumEntryName")
    enum class Model(val model: String) {
        Gemini1_5Pro("gemini-1.5-pro-latest")
    }
}