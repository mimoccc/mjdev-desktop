package org.psippr.desktop.managers.ai.tts

import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.managers.ai.tts.base.TTSPlugin

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
