/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.artificialintelligence.plugins

import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.content
import eu.mjdev.desktop.extensions.Custom.loadKey
import eu.mjdev.desktop.managers.artificialintelligence.base.AIPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

/*
* Gemini key : https://makersuite.google.com/app/apikey
* */
class AiPluginGemini(
    val scope: CoroutineScope
) : AIPlugin {
    private val key = loadKey("gemini")
    private val generativeModel: GenerativeModel? by lazy {
        if (key.isNotEmpty()) {
            GenerativeModel(
                modelName = "gemini-1.5-pro-latest",
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
                generativeModel?.generateContent(content {
                    text(question)
                })?.text
            }
        }.onFailure { e ->
            error = e
        }.getOrNull() ?: error?.message ?: ""
    }.await()
}