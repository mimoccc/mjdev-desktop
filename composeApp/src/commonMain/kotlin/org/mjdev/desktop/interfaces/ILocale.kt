package org.mjdev.desktop.interfaces

interface ILocale {
    val country: String
    val displayName: String

    companion object {
        fun from(
            country: String,
            displayName: String
        ): ILocale = object : ILocale {
            override val country: String = country
            override val displayName: String = displayName
        }

        val DEFAULT = from("US", "EN")
    }
}