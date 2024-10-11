/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.artificialintelligence.stt

import eu.mjdev.desktop.managers.artificialintelligence.stt.base.STTPlugin
import eu.mjdev.desktop.managers.artificialintelligence.stt.base.STTListener
import kotlinx.coroutines.CoroutineScope

@Suppress("UNUSED_PARAMETER")
class STTPluginNull(scope: CoroutineScope) : STTPlugin {
    override val listeners: List<STTListener> = mutableListOf()
}