package org.psippr.desktop.managers.ai.tts

import nl.marc_apps.tts.TextToSpeechFactory
import nl.marc_apps.tts.experimental.ExperimentalDesktopTarget
import org.psippr.desktop.context.IDesktopContext

@OptIn(ExperimentalDesktopTarget::class)
class TTSPluginDesktop(
    context: IDesktopContext,
) : TTSPluginMain(context, {
        TextToSpeechFactory().createOrNull()
    }) {
    // todo
    override val isPresent: Boolean = true
}
