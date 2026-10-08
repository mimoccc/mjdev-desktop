/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.psippr.desktop.managers.ai.stt

import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.managers.ai.stt.base.STTListener
import org.psippr.desktop.managers.ai.stt.base.STTPlugin

@Suppress("UNUSED_PARAMETER")
class STTPluginEmpty(
    context: IDesktopContext,
) : STTPlugin {
    override val listeners: List<STTListener> = mutableListOf()
}
