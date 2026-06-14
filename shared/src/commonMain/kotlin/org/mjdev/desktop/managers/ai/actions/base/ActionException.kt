/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.ai.actions.base

@Suppress("unused")
open class ActionException(
    message: String = "",
    t: Throwable? = null,
    val isError: Boolean = false,
) : Exception(message, t) {
    object ActionNone : ActionException(
        isError = false,
    )

    class ActionSuccess(
        message: String = "",
        t: Throwable? = null,
    ) : ActionException(message, t, isError = false)

    class ActionFail(
        message: String = "",
        t: Throwable? = null,
    ) : ActionException(message, t, true) {
        constructor(t: Throwable) : this(t.message ?: "Error", t)
    }
}
