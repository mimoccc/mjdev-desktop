package org.mjdev.desktop.extensions

import okio.Path
import org.mjdev.desktop.extensions.PathExt.absolutePath
import org.mjdev.desktop.helpers.gif.GifImage

object MimeTypeExt {
    // todo from mime
    val String.isGif get() = trim().endsWith(".gif")

    // todo from mime
    val String.isVideo get() =
        trim().let {
            it.endsWith(".mp4") || it.endsWith(".mpg")
        }

    // todo from mime
    val Path.isGif
        get() = absolutePath.isGif

    // todo from mime
    val Path.isVideo
        get() = absolutePath.isGif

    // todo from mime
    val Any.isGif get() =
        when (this) {
            is String -> this.isGif
            is Path -> this.isGif
            is GifImage -> true
            else -> false
        }

    // todo from mime
    val Any.isVideo get() =
        when (this) {
            is String -> this.isVideo
            is Path -> this.isVideo
            is GifImage -> false
            else -> false
        }
}
