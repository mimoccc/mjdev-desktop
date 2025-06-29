package org.mjdev.desktop.managers.ai.plugins

import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.managers.ai.plugins.base.AIPlugin
import community.flock.aigentic.core.agent.tool.Result.Stuck
import community.flock.aigentic.core.agent.tool.Result.Fatal
import community.flock.aigentic.core.agent.tool.Result.Finished
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.openai.dsl.openAIModel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier
import community.flock.aigentic.openai.model.OpenAIModelIdentifier.GPT4OMini
import org.mjdev.desktop.log.Log

// todo tools = actions
// Open AI keys: https://platform.openai.com/api-keys
class AiPluginOpenAi(
    private val context: IDesktopContext,
    private val apiKey: String = context.keysManager.loadKey("open-ai"),
    private val model: OpenAIModelIdentifier = GPT4OMini,
) : AIPlugin {

    override suspend fun ask(
        question: String
    ): String = agent {
//        Log.d("Using api key : $apiKey")
        openAIModel {
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
