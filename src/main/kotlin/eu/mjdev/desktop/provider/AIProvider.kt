package eu.mjdev.desktop.provider

import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.content
import eu.mjdev.desktop.extensions.Custom.loadKey
import kotlinx.coroutines.runBlocking

@Suppress("MemberVisibilityCanBePrivate", "unused")
class AIProvider(
    var plugin: IAIPlugin = AiPluginNull()
) {
    fun ask(question: String): String = plugin.ask(question)

    interface IAIPlugin {
        fun ask(question: String): String
    }

    class AiPluginNull : IAIPlugin {
        override fun ask(question: String): String = ""
    }

    class AiPluginGemini : IAIPlugin {
        private val generativeModel: GenerativeModel by lazy {
            GenerativeModel(
                modelName = "gemini-1.5-pro-latest",
                apiKey = loadKey("gemini")
            )
        }

        override fun ask(question: String): String = runBlocking {
            runCatching {
                generativeModel.generateContent(content {
                    text(question)
                }).text
            }.onFailure {
                println(it)
            }.getOrElse {
                ""
            }
        }.orEmpty()
    }
}