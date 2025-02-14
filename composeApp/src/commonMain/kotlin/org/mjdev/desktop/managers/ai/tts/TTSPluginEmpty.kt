package org.mjdev.desktop.managers.ai.tts.base

import org.mjdev.desktop.interfaces.IDesktopContext

@Suppress("UNUSED_PARAMETER")
class TTSPluginEmpty(
    context: IDesktopContext
) : TTSPlugin {
    override fun talk(text: String, clearQueue: Boolean) {
    }
}