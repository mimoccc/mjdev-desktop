/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.ai.stt

import org.mjdev.desktop.managers.ai.stt.base.STTPlugin
import org.mjdev.desktop.managers.ai.stt.base.STTListener
import org.mjdev.desktop.interfaces.IDesktopContext

@Suppress("UNUSED_PARAMETER")
class STTPluginEmpty(
    context: IDesktopContext
) : STTPlugin {
    override val listeners: List<STTListener> = mutableListOf()
}