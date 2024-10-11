/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.artificialintelligence.actions

import eu.mjdev.desktop.managers.artificialintelligence.actions.base.Action
import eu.mjdev.desktop.managers.artificialintelligence.actions.base.ActionException
import eu.mjdev.desktop.managers.artificialintelligence.actions.base.ActionException.ActionFail
import eu.mjdev.desktop.managers.artificialintelligence.actions.base.ActionException.ActionSuccess
import eu.mjdev.desktop.managers.artificialintelligence.actions.base.ActionProviderScope
import eu.mjdev.desktop.provider.DesktopProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

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

    // todo regexp & fuzzy
    fun tryAction(
        text: String
    ): ActionException {
        return text.trim().let { t ->
            firstOrNull { a ->
                a.text.contentEquals(t, true) || a.text.contains(t, true)
            }?.let { a ->
                a.lastSeen = System.currentTimeMillis()
                runBlocking(Dispatchers.IO) {
                    a.action.invoke(ActionProviderScope(api)).let { r ->
                        if(r is ActionFail) r else ActionSuccess(a.responseSuccess, r)
                    }
                }
            } ?: ActionException.ActionNone
        }
    }
}