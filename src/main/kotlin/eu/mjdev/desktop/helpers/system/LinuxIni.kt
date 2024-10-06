/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

@file:Suppress("DEPRECATION")

package eu.mjdev.desktop.helpers.system

import org.ini4j.Config
import org.ini4j.Wini
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.StringBufferInputStream
import java.nio.charset.Charset

@Suppress("unused")
class LinuxIni(
    stream: InputStream
) : Wini(stream) {
    constructor() : this("")
    constructor(data: String) : this(StringBufferInputStream(data))
    constructor(file: File) : this(FileInputStream(file))

    init {
        config = Config().apply {
            fileEncoding = Charset.defaultCharset()
            isPropertyFirstUpper = true
            isEscape = false
            isStrictOperator = true
        }
    }
}