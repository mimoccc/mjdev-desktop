/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.application.l10n

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.PlatformLocalization

fun translationFor(localeTag: String) = when(localeTag) {
    "" -> Translations.en()
    "en_AU" -> Translations.enAU()
    "en_CA" -> Translations.enCA()
    "en_GB" -> Translations.enGB()
    "en_IN" -> Translations.enIN()
    else -> null
}

@Composable
fun defaultPlatformLocalization(): PlatformLocalization {
    val copy = getString(Strings.Copy)
    val cut = getString(Strings.Cut)
    val paste = getString(Strings.Paste)
    val selectAll = getString(Strings.SelectAll)
    return object : PlatformLocalization {
        override val copy = copy
        override val cut = cut
        override val paste = paste
        override val selectAll = selectAll
    }
}