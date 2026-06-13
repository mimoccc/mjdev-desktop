/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.ai.plugins

import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.managers.ai.plugins.base.AIPlugin

@Suppress("UNUSED_PARAMETER")
class AiPluginEmpty(
    context: IDesktopContext,
) : AIPlugin {
    override suspend fun ask(question: String): String = "No a.i. provider selected please read manual."
}
