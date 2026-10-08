/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.psippr.desktop.managers.ai.actions.base

import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.managers.ai.IAiManager
import org.psippr.desktop.managers.ai.actions.base.ActionException.ActionFail
import org.psippr.desktop.managers.ai.actions.base.ActionException.ActionSuccess

@Suppress("RedundantSuspendModifier", "unused")
class ActionProviderScope(
    private val context: IDesktopContext,
    private val ai: IAiManager = context.ai,
) {
    suspend fun success(text: String) = ActionSuccess(text)

    // todo actions
    suspend fun open(what: String): ActionException = try {
        context.open(what)
        ActionSuccess()
    } catch (e: Exception) {
        ActionFail(e)
    }

    suspend fun say(
        what: String,
        clearQueue: Boolean = false,
    ): ActionException = try {
        ai.say(what, clearQueue)
        ActionSuccess()
    } catch (e: Exception) {
        ActionFail(e)
    }
}
