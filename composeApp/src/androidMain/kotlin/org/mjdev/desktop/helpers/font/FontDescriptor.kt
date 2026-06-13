package org.mjdev.desktop.helpers.font

import android.annotation.SuppressLint
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import org.mjdev.desktop.helpers.streams.ResourceStream.Companion.openResource
import java.io.File

@SuppressLint("DiscouragedApi")
class FontDescriptor(
    ttfFileName: String,
) {
    private val fontTypeFace: Typeface? =
        runCatching {
            val tempFile = File.createTempFile("font", ".ttf")
            openResource(ttfFileName).use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Typeface.createFromFile(tempFile)
        }.getOrNull()

    val fontFamily: FontFamily =
        fontTypeFace?.let { typeFace ->
            FontFamily(typeFace)
        } ?: FontFamily.Default
}
