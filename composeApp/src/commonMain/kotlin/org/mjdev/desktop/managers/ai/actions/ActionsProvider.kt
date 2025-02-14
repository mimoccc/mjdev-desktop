/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.ai.actions

import org.mjdev.desktop.log.Log
import org.mjdev.desktop.managers.ai.actions.base.Action
import org.mjdev.desktop.managers.ai.actions.base.ActionException
import org.mjdev.desktop.managers.ai.actions.base.ActionException.ActionFail
import org.mjdev.desktop.managers.ai.actions.base.ActionException.ActionSuccess
import org.mjdev.desktop.managers.ai.actions.base.ActionProviderScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.mjdev.desktop.extensions.Compose.addIfNotExists
import org.mjdev.desktop.extensions.System.currentTimeMillis
import org.mjdev.desktop.helpers.fuzzywuzzy.FuzzySearch
import org.mjdev.desktop.interfaces.IDesktopContext

@Suppress("unused")
class ActionsProvider(
    val context: IDesktopContext,
) {
    val actions = mutableListOf<Action>()
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    constructor(
        context: IDesktopContext,
        block: ActionsProvider.() -> Unit = {}
    ) : this(context) {
        block.invoke(this)
    }

    constructor(
        context: IDesktopContext,
        vararg actions: Action
    ) : this(context) {
        this@ActionsProvider.actions.addAll(actions)
    }

    // todo infix fnc or etc
    fun action(
        command: String,
        action: suspend ActionProviderScope.() -> ActionException,
    ) {
        actions.add(
            Action(
                name = command,
                text = command,
                action = action
            )
        )
    }

    // todo regexp
    suspend fun tryAction(
        text: String
    ): ActionException {
        return text.trim().lowercase().let { t ->
            var action = actions.firstOrNull { a ->
                a.text.contentEquals(t, true) || a.history.contains(t)
            }
            if (action != null) {
                Log.i("got action ${action}, relevance: exact.")
            } else {
                val fuzzy: Pair<Int, Action>? = actions.map { a ->
                    Pair(FuzzySearch.ratio(a.text, t), a)
                }.maxByOrNull { p ->
                    p.first
                }?.let { p -> if (p.first > 70) p else null }
                action = fuzzy?.second
                if (action != null) {
                    Log.i("got action ${fuzzy?.second}, relevance: ${fuzzy?.first}.")
                }
            }
            if (action != null) {
                action.lastSeen = currentTimeMillis()
                action.history.addIfNotExists(t) { t1, t2 -> t1.contentEquals(t2, true) }
                action.action.invoke(ActionProviderScope(context)).let { r ->
                    if (r is ActionFail) r
                    else ActionSuccess(action.responseSuccess, r)
                }
            } else {
                ActionException.ActionNone
            }
        }
    }
}
