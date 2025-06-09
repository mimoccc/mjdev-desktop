package org.mjdev.desktop.managers.ai.plugins

import kotlinx.coroutines.CoroutineScope
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.managers.ai.plugins.base.AIPlugin

class AiPluginOllama(
    private val context: IDesktopContext,
    private val scope: CoroutineScope = context.scope
) : AIPlugin {
//    private val ollama by lazy { null }

    override suspend fun ask(question: String): String {
//        ollama.setModel("")
        return "" // todo
    }
}