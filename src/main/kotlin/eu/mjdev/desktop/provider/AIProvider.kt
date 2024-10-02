package eu.mjdev.desktop.provider

import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.content
import eu.mjdev.desktop.extensions.Custom.loadKey
import eu.mjdev.desktop.helpers.system.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeechFactory
import nl.marc_apps.tts.experimental.ExperimentalDesktopTarget
/*
* Gemini key : https://makersuite.google.com/app/apikey
* */
@Suppress("MemberVisibilityCanBePrivate", "unused", "UNUSED_PARAMETER")
class AIProvider(
    val scope: CoroutineScope,
    var pluginAI: IAIPlugin = AiPluginNull(scope),
    val pluginSTT: ISTTPlugin = STTPluginNull(scope),
    val pluginTTS: TTSPlugin = TTSPluginMain(scope),
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

    interface STTListener {
        fun onTextRecognized(text: String)
    }

    interface ISTTPlugin {
        val listeners: List<STTListener>
    }

    interface TTSPlugin {
        fun talk(text: String, clearQueue: Boolean = false)
    }

    class STTPluginNull(scope: CoroutineScope) : ISTTPlugin {
        override val listeners: List<STTListener> = mutableListOf()
    }

    @OptIn(ExperimentalDesktopTarget::class)
    class TTSPluginMain(
        val scope: CoroutineScope
    ) : TTSPlugin {
        private val textToSpeech = scope.async {
            TextToSpeechFactory().createOrNull()
        }

        override fun talk(text: String, clearQueue: Boolean) {
            println("talking: $text")
            scope.launch {
                textToSpeech.await()?.say(text, clearQueue)
            }
        }
    }

    class TTSPluginSwift(
        val scope: CoroutineScope
    ) : TTSPlugin {
        override fun talk(text: String, clearQueue: Boolean) {
            println("talking: $text")
            scope.launch(Dispatchers.IO) {
                Shell.executeAndRead(
                    "/opt/swift/bin/swift",
                    "\"${text.replace("\"", " ")}\""
                )
            }
        }
    }

    class AiPluginNull(scope: CoroutineScope) : IAIPlugin {
        override suspend fun ask(question: String): String = ""
    }

    class AiPluginGemini(
        val scope: CoroutineScope
    ) : IAIPlugin {
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
}