package org.mjdev.desktop.managers.ai.plugins

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.llm.LLModel
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.managers.ai.plugins.base.AIPlugin

// todo tools = actions
// Open AI keys: https://platform.openai.com/api-keys
class AiPluginOpenAi(
    private val context: IDesktopContext,
    private val apiKey: String = context.keysManager.loadKey("open-ai"),
    private val model: LLModel = OpenAIModels.Chat.GPT4o,
) : AIPlugin {
    val agent by lazy {
        runCatching {
            AIAgent(
                executor = simpleOpenAIExecutor(apiKey),
                systemPrompt = "You are a helpful assistant. Answer user questions concisely.",
                llmModel = model
            )
        }
    }

    @Suppress("USELESS_CAST")
    override suspend fun ask(
        question: String
    ): String = runCatching {
        agent.getOrThrow().let { agent ->
            (agent as AIAgent<String, String>).run("Hello! How can you help me?")
        }
    }.getOrElse { e ->
        "Error at: ${e.stackTraceToString()}"
    }
}
