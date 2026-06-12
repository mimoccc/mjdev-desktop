package org.mjdev.desktop.managers.palette

import androidx.compose.ui.graphics.Color
import org.mjdev.desktop.extensions.Colors.SuperDarkGray
import org.mjdev.desktop.interfaces.IDisposable
import org.mjdev.desktop.managers.base.IDelegate

interface IPalette : IDisposable, IDelegate {
    val baseColor: Color
    val backgroundColor: Color
    val textColor: Color
    val iconsTintColor: Color
    val borderColor: Color
    val disabledColor: Color
    val selectedBgColor: Color
    val selectedFgColor: Color
    val focusBorderColor: Color
    val focusedTextBackgroundColor: Color
    val tooltipBgColor: Color
    val tooltipFgColor: Color

    fun update(src: Any?)

    companion object {
        val EMPTY = object : IPalette {
            override val baseColor: Color = Color.SuperDarkGray
            override val backgroundColor: Color = Color.SuperDarkGray
            override val textColor: Color = Color.White
            override val iconsTintColor: Color = Color.White
            override val borderColor: Color = Color.White
            override val disabledColor: Color = Color.SuperDarkGray
            override val selectedBgColor: Color = Color.White
            override val selectedFgColor: Color = Color.SuperDarkGray
            override val focusBorderColor: Color = Color.White
            override val focusedTextBackgroundColor: Color = Color.SuperDarkGray
            override val tooltipBgColor: Color = Color.SuperDarkGray
            override val tooltipFgColor: Color = Color.White

            override fun update(src: Any?) {}
            override fun dispose() {}
        }
    }
}