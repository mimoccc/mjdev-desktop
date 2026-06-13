package org.mjdev.desktop.managers.ai.tts

import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.managers.ai.tts.base.TTSPlugin

@Suppress("UNUSED_PARAMETER")
class TTSPluginEmpty(
    context: IDesktopContext,
) : TTSPlugin {
    override val isPresent: Boolean = true

    override fun talk(
        text: String,
        clearQueue: Boolean,
    ) {
    }
}
