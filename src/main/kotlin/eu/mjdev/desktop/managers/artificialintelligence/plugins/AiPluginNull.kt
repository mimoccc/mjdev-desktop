/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.artificialintelligence.plugins

import eu.mjdev.desktop.managers.artificialintelligence.base.AIPlugin
import kotlinx.coroutines.CoroutineScope

@Suppress("UNUSED_PARAMETER")
class AiPluginNull(scope: CoroutineScope) : AIPlugin {
    override suspend fun ask(question: String): String =
        "No a.i. provider selected please read manual."
}