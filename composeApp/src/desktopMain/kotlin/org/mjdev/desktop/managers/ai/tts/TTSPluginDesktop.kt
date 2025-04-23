package org.mjdev.desktop.managers.ai.tts

import nl.marc_apps.tts.TextToSpeechFactory
import nl.marc_apps.tts.experimental.ExperimentalDesktopTarget
import org.mjdev.desktop.interfaces.IDesktopContext

@OptIn(ExperimentalDesktopTarget::class)
class TTSPluginDesktop(
    context: IDesktopContext,
) : TTSPluginMain(context, {
    TextToSpeechFactory().createOrNull()
}) {
    // todo
    override val isPresent: Boolean = true
}