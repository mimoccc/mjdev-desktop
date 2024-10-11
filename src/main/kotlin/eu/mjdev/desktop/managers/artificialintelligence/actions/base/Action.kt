/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.artificialintelligence.actions.base

// todo history with command strings to analyze best result
@Suppress("unused")
class Action(
    val name: String,
    val text: String,
    val regExp: String? = null,
    val responseSuccess: String = "OK",
    val action: suspend ActionProviderScope.() -> ActionException,
    var lastSeen: Long = 0
//    val history : ActionHistory
)
