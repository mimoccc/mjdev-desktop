package org.mjdev.desktop.managers.ai.actions

val ActionsProvider.ActionCalculator
    get() = action(
        "open calculator"
    ) {
        open("calculator")
    }