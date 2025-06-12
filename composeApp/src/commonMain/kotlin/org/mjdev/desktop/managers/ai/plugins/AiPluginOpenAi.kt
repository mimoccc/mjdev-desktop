package org.mjdev.desktop.managers.ai.plugins

import kotlinx.coroutines.CoroutineScope
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.managers.ai.plugins.base.AIPlugin
import kotlin.time.Duration.Companion.seconds
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import org.mjdev.desktop.log.Log

// todo test
class AiPluginOpenAi(
    private val context: IDesktopContext,
    private val scope: CoroutineScope = context.scope
) : AIPlugin {
    private val apiKey = context.keysManager.loadKey("open-ai")
    private val modelId = ModelId("gpt-3.5-turbo")
    private val openAI: OpenAI? = runCatching {
        if (apiKey.isNotEmpty()) OpenAI(
            OpenAIConfig(
                token = apiKey,
                logLevel = LogLevel.All,
                logger = Logger.Simple,
                timeout = Timeout(socket = 60.seconds),
            )
        ) else {
            Log.w("OpenAI API key is not set, using empty OpenAI client.")
            null
        }
    }.onFailure { e ->
        Log.e(e)
    }.getOrNull()

    @OptIn(BetaOpenAI::class)
    override suspend fun ask(question: String): String = scope.async {
        runCatching {
            openAI?.chatCompletions(
                ChatCompletionRequest(
                    model = modelId,
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.User,
                            content = question
                        )
                    ),
                    maxTokens = 1000,
                    temperature = 0.0
                )
            )?.first().let { chunk ->
                if (chunk is ChatCompletionChunk) {
                    chunk.choices.firstOrNull()?.delta?.content ?: ""
                } else {
                    ""
                }
            }
        }.onFailure { e ->
            Log.e(e)
        }.getOrDefault("")
    }.await()
}