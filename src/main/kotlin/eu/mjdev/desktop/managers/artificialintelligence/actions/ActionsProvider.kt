/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.artificialintelligence.actions

import eu.mjdev.desktop.extensions.Custom.addIfNotExists
import eu.mjdev.desktop.managers.artificialintelligence.actions.base.Action
import eu.mjdev.desktop.managers.artificialintelligence.actions.base.ActionException
import eu.mjdev.desktop.managers.artificialintelligence.actions.base.ActionException.ActionFail
import eu.mjdev.desktop.managers.artificialintelligence.actions.base.ActionException.ActionSuccess
import eu.mjdev.desktop.managers.artificialintelligence.actions.base.ActionProviderScope
import eu.mjdev.desktop.provider.DesktopProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import me.xdrop.fuzzywuzzy.FuzzySearch

@Suppress("unused")
class ActionsProvider(
    val api: DesktopProvider,
) : ArrayList<Action>() {
    val scope: CoroutineScope = api.scope

    constructor(
        api: DesktopProvider,
        block: ActionsProvider.() -> Unit = {}
    ) : this(api) {
        block.invoke(this)
    }

    constructor(
        api: DesktopProvider,
        vararg actions: Action
    ) : this(api) {
        addAll(actions)
    }

    fun action(
        command: String,
        action: suspend ActionProviderScope.() -> ActionException,
    ) {
        add(
            Action(
                name = command,
                text = command,
                action = action
            )
        )
    }

    // todo regexp
    fun tryAction(
        text: String
    ): ActionException {
        return text.trim().lowercase().let { t ->
            var action = firstOrNull { a ->
                a.text.contentEquals(t, true) || a.history.contains(t)
            }
            if (action != null) {
                println("got action ${action}, relevance: exact.")
            } else {
                val fuzzy: Pair<Int, Action>? = map { a ->
                    Pair(FuzzySearch.ratio(a.text, t), a)
                }.maxByOrNull { p ->
                    p.first
                }?.let { p -> if (p.first > 70) p else null }
                action = fuzzy?.second
                if (action != null) {
                    println("got action ${fuzzy?.second}, relevance: ${fuzzy?.first}.")
                }
            }
            if (action != null) {
                action.lastSeen = System.currentTimeMillis()
                action.history.addIfNotExists(t) { t1, t2 -> t1.contentEquals(t2, true) }
                runBlocking(Dispatchers.IO) {
                    action.action.invoke(ActionProviderScope(api)).let { r ->
                        if (r is ActionFail) r
                        else ActionSuccess(action.responseSuccess, r)
                    }
                }
            } else {
                ActionException.ActionNone
            }
        }
    }
}