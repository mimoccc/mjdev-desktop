/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.ai.plugins

import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.Result.Fatal
import community.flock.aigentic.core.agent.tool.Result.Finished
import community.flock.aigentic.core.agent.tool.Result.Stuck
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import community.flock.aigentic.gemini.model.GeminiModelIdentifier.Gemini1_5Pro
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.managers.ai.plugins.base.AIPlugin

// todo tools = actions
// Gemini key : https://makersuite.google.com/app/apikey
class AiPluginGemini(
    private val context: IDesktopContext,
    private val apiKey: String = context.keysManager.loadKey("gemini"),
    private val model: GeminiModelIdentifier = Gemini1_5Pro,
) : AIPlugin {

    override suspend fun ask(
        question: String
    ): String = agent {
//        Log.d("Using api key : $apiKey")
        geminiModel {
            apiKey(apiKey)
            modelIdentifier(model)
        }
        task("Provide information") {
            addInstruction("Respond to user queries with relevant information.")
        }
        context {
            addText(question)
        }
        finishResponse()
    }.start().result.let { result ->
        when (result) {
            is Finished<*> -> result.response.toString()
            is Stuck -> result.reason
            is Fatal -> result.message
        }
    }

}
