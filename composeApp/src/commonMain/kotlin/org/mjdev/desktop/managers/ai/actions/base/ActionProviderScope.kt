/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.ai.actions.base

import org.mjdev.desktop.managers.ai.actions.base.ActionException.ActionFail
import org.mjdev.desktop.managers.ai.actions.base.ActionException.ActionSuccess
import org.mjdev.desktop.interfaces.IDesktopContext

class ActionProviderScope(
    private val context: IDesktopContext
) {
    // todo actions
    suspend fun open(
        what: String
    ): ActionException = try {
        context.open(what)
        ActionSuccess()
    } catch (e: Exception) {
        ActionFail(e)
    }
}
