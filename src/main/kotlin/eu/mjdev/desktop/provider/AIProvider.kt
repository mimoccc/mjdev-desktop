package eu.mjdev.desktop.provider

import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.content
import eu.mjdev.desktop.extensions.Custom.loadKey
import eu.mjdev.desktop.helpers.system.Command
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeechFactory
import nl.marc_apps.tts.experimental.ExperimentalDesktopTarget

@Suppress("MemberVisibilityCanBePrivate", "unused", "UNUSED_PARAMETER", "RemoveEmptyClassBody")
class AIProvider(
    val scope: CoroutineScope,
    var pluginAI: IAIPlugin = AiPluginNull(scope),
    val pluginSTT: ISTTPlugin = STTPluginNull(scope),
    val pluginTTS: TTSPlugin = TTSPluginSwift(scope),
    val isAvailable: () -> Boolean = { pluginAI !is AiPluginNull }
) {
    fun ask(
        question: String,
        block: AIProvider.(question: String, result: String) -> Unit
    ) = scope.launch {
        pluginAI.ask(question).also { result ->
            block.invoke(this@AIProvider, question, result)
        }
    }

    fun talk(text: String, clearQueue: Boolean = false) =
        pluginTTS.talk(text, clearQueue)

    interface IAIPlugin {
        suspend fun ask(question: String): String
    }

    interface ISTTPlugin {
    }

    interface TTSPlugin {
        fun talk(text: String, clearQueue: Boolean = false)
    }

    class STTPluginNull(scope: CoroutineScope) : ISTTPlugin {
    }

    @OptIn(ExperimentalDesktopTarget::class)
    class TTSPluginMain(
        val scope: CoroutineScope
    ) : TTSPlugin {
        private val textToSpeech = scope.async {
            TextToSpeechFactory().createOrNull()
        }

        override fun talk(text: String, clearQueue: Boolean) {
            scope.launch {
                textToSpeech.await()?.say(text, clearQueue)
            }
        }
    }

    class TTSPluginSwift(
        val scope: CoroutineScope
    ) : TTSPlugin {
        override fun talk(text: String, clearQueue: Boolean) {
            scope.launch(Dispatchers.IO) {
                Command(
                    "/opt/swift/bin/swift",
                    "\"${text.replace("\"", " ")}\""
                ).execute()
            }
        }
    }

    class AiPluginNull(scope: CoroutineScope) : IAIPlugin {
        override suspend fun ask(question: String): String = ""
    }

    class AiPluginGemini(
        val scope: CoroutineScope
    ) : IAIPlugin {
        private val generativeModel: GenerativeModel by lazy {
            GenerativeModel(
                modelName = "gemini-1.5-pro-latest",
                apiKey = loadKey("gemini")
            )
        }

        override suspend fun ask(question: String): String = scope.async {
            var error: Throwable? = null
            runCatching {
                generativeModel.generateContent(content {
                    text(question)
                }).text
            }.onFailure { e ->
                error = e
            }.getOrElse {
                error?.message ?: ""
            } ?: ""
        }.await()
    }
}