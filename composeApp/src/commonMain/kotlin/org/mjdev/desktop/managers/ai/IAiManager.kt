package org.mjdev.desktop.managers.ai

import org.mjdev.desktop.managers.base.IDelegate

interface IAiManager : IDelegate {
    val isAvailable: () -> Boolean

    fun ask(
        question: String,
        block: IAiManager.(question: String, result: String) -> Unit = { _, r -> say(r) }
    )

    fun say(
        text: String,
        clearQueue: Boolean = false
    )

    companion object {
        val EMPTY = object : IAiManager {
            override val isAvailable: () -> Boolean = { false }
            override fun ask(question: String, block: IAiManager.(question: String, result: String) -> Unit) {}
            override fun say(text: String, clearQueue: Boolean) {}
        }
    }
}
