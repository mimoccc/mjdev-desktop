package org.mjdev.desktop.managers.ai.actions

import org.mjdev.desktop.managers.ai.actions.base.ActionsProvider

val ActionsProvider.ActionCalculator
    get() =
        action(
            "open calculator",
        ) {
            open("calculator")
        }
