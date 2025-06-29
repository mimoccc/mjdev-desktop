package org.mjdev.desktop.managers.ai.plugins

import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.Result.Fatal
import community.flock.aigentic.core.agent.tool.Result.Finished
import community.flock.aigentic.core.agent.tool.Result.Stuck
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.ollama.dsl.ollamaModel
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.managers.ai.plugins.base.AIPlugin

// todo tools = actions
// todo test with ollama model
class AiPluginOllama(
    private val context: IDesktopContext,
    private val apiKey: String = context.keysManager.loadKey("ollama"),
) : AIPlugin {

    override suspend fun ask(
        question: String
    ): String = agent {
        ollamaModel {
            apiUrl(apiKey)
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
