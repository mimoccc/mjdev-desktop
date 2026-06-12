package org.mjdev.desktop.extensions

import kotlin.jvm.JvmInline

@JvmInline
value class PointerButton(val index: Int) {
    companion object {
        val Primary = PointerButton(0)
        val Secondary = PointerButton(1)
        val Tertiary = PointerButton(2)
        val Back = PointerButton(3)
        val Forward = PointerButton(4)
    }
}

val PointerButton?.isPrimary: Boolean
    get() { return this == PointerButton.Primary }

val PointerButton?.isSecondary: Boolean
    get() { return this == PointerButton.Secondary }

val PointerButton?.isTertiary: Boolean
    get() { return this == PointerButton.Tertiary }

val PointerButton?.isBack: Boolean
    get() { return this == PointerButton.Back }

val PointerButton?.isForward: Boolean
    get() { return this == PointerButton.Forward }