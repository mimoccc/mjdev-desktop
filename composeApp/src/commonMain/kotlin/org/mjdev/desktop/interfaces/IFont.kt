package org.mjdev.desktop.interfaces

import androidx.compose.ui.text.font.FontFamily

interface IFont {
    val fontFamily: FontFamily

    fun iconForName(iconName: String?): Int?

    companion object {
        val Empty =
            object : IFont {
                override val fontFamily: FontFamily = FontFamily.Default

                override fun iconForName(iconName: String?): Int? = null
            }
    }
}
