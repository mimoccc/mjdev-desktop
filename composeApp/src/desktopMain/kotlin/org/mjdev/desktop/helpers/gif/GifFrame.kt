package org.mjdev.desktop.helpers.gif

import java.awt.image.BufferedImage
import kotlin.jvm.JvmField

class GifFrame(
    @JvmField val image: BufferedImage?,
    @JvmField val delay: Int
)