/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.artificialintelligence.actions.base

import eu.mjdev.desktop.managers.artificialintelligence.actions.base.ActionException.ActionFail
import eu.mjdev.desktop.managers.artificialintelligence.actions.base.ActionException.ActionSuccess
import eu.mjdev.desktop.provider.DesktopProvider

class ActionProviderScope(
    private val api: DesktopProvider
) {
    // todo actions
    suspend fun open(
        what: String
    ): ActionException = try {
        api.open(what)
        ActionSuccess()
    } catch (e: Exception) {
        ActionFail(e)
    }
}
