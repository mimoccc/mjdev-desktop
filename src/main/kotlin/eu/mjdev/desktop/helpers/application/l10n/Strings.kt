/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.application.l10n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.intl.Locale

@Immutable
@JvmInline
value class Strings private constructor(@Suppress("unused") private val value: Int) {
    companion object {
        val Copy = Strings(0)
        val Cut = Strings(1)
        val Paste = Strings(2)
        val SelectAll = Strings(3)
        // When adding values here, make sure to also add them in ui/build.gradle,
        // updateTranslations task (stringByResourceName parameter), and re-run the task
    }
}

@Composable
@ReadOnlyComposable
fun getString(string: Strings): String {
    val locale = Locale.current
    val tag = localeTag(language = locale.language, region = locale.region)
    val translation = translationByLocaleTag.getOrPut(tag) {
        findTranslation(locale)
    }
    return translation[string] ?: error("Missing translation for $string")
}

typealias Translation = Map<Strings, String>

val translationByLocaleTag = mutableMapOf<String, Translation>()

fun localeTag(language: String, region: String) = when {
    language == "" -> ""
    region == "" -> language
    else -> "${language}_$region"
}

fun localeTagChain(locale: Locale) = sequence {
    if (locale.region != "") {
        yield(localeTag(language = locale.language, region = locale.region))
    }
    if (locale.language != "") {
        yield(localeTag(language = locale.language, region = ""))
    }
    yield(localeTag("", ""))
}

fun findTranslation(locale: Locale): Map<Strings, String> {
    // We don't need to merge translations because each one should contain all the strings.
    return localeTagChain(locale).firstNotNullOf { translationFor(it) }
}

object Translations