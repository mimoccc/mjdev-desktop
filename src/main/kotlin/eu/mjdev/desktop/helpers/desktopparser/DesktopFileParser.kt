/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

@file:Suppress("DEPRECATION")

package eu.mjdev.desktop.helpers.desktopparser

import org.ini4j.Config
import org.ini4j.Wini
import java.io.InputStream
import java.io.StringBufferInputStream
import java.nio.charset.Charset

class DesktopFileParser(
    stream: InputStream,
    charset: Charset = Charsets.UTF_8
) : Wini(stream) {
    constructor() : this("")
    constructor(data: String) : this(StringBufferInputStream(data))

    init {
        config = Config().apply {
            fileEncoding = Charset.defaultCharset()
            isPropertyFirstUpper = true
            isEscape = false
            isStrictOperator = true
            fileEncoding = charset
            isEmptyOption = true
            isEmptySection = true
        }
    }

    override fun load(
        input: InputStream?
    ) {
        try {
            super.load(input)
        } catch (e: Exception) {
//            throw(Exception("Error in file: file:///${file.absolutePath}", e))
        }
    }
}
