/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.helpers.gif

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL

@Suppress("unused", "DEPRECATION")
class GifDecoder {
    companion object {
        const val STATUS_OK = 0
        const val STATUS_FORMAT_ERROR = 1
        const val STATUS_OPEN_ERROR = 2
    }

    private var status: Int = STATUS_OK
    private var width: Int = 0
    private var height: Int = 0
    private var gctSize: Int = 0
    private var loopCount: Int = 1
    private var bgIndex: Int = 0
    private var bgColor: Int = 0
    private var lastBgColor: Int = 0
    private var pixelAspect: Int = 0
    private var lctSize: Int = 0
    private var ix: Int = 0
    private var iy: Int = 0
    private var iw: Int = 0
    private var ih: Int = 0
    private var blockSize: Int = 0
    private var dispose: Int = 0
    private var lastDispose: Int = 0
    private var delay: Int = 0
    private var transIndex: Int = 0
    private val maxStackSize = 4096

    private var transparency: Boolean = false
    private var lctFlag: Boolean = false
    private var interlace: Boolean = false
    private var gctFlag: Boolean = false

    private var prefix: ShortArray = shortArrayOf()
    private var suffix: ByteArray = byteArrayOf()
    private var pixelStack: ByteArray = byteArrayOf()
    private var pixels: ByteArray = byteArrayOf()
    private var lastRect: Rectangle = Rectangle()

    private var gct: IntArray = intArrayOf()
    private var lct: IntArray = intArrayOf()
    private var act: IntArray = intArrayOf()

    private var image: BufferedImage? = null
    private var lastImage: BufferedImage? = null
    private var input: BufferedInputStream? = null

    private val block = ByteArray(256)
    private var frames = ArrayList<GifFrame>()
    private var frameCount: Int = 0

    fun getDelay(n: Int): Int {
        delay = -1
        if (n in 0..<frameCount) {
            delay = frames[n].delay
        }
        return delay
    }

    fun getFrameCount(): Int = frameCount

    fun getImage(): BufferedImage? = getFrame(0)

    fun getLoopCount(): Int = loopCount

    private fun setPixels() {
        val dest = (image?.raster?.dataBuffer as DataBufferInt).data ?: intArrayOf()
        if (lastDispose > 0) {
            if (lastDispose == 3) {
                val n = frameCount - 2
                lastImage = if (n > 0) getFrame(n - 1) else null
            }
            if (lastImage != null) {
                val prev = (lastImage?.raster?.dataBuffer as DataBufferInt).data ?: intArrayOf()
                System.arraycopy(prev, 0, dest, 0, width * height)

                if (lastDispose == 2) {
                    val g = image!!.createGraphics()
                    val c: Color = if (transparency) Color(0, 0, 0, 0) else Color(lastBgColor)
                    g.color = c
                    g.composite = AlphaComposite.Src
                    g.fillRect(lastRect.x, lastRect.y, lastRect.width, lastRect.height)
                    g.dispose()
                }
            }
        }
        var pass = 1
        var inc = 8
        var iLine = 0
        for (i in 0 until ih) {
            var line = i
            if (interlace) {
                if (iLine >= ih) {
                    pass++
                    when (pass) {
                        2 -> iLine = 4
                        3 -> {
                            iLine = 2
                            inc = 4
                        }

                        4 -> {
                            iLine = 1
                            inc = 2
                        }
                    }
                }
                line = iLine
                iLine += inc
            }
            line += iy
            if (line < height) {
                val k = line * width
                var dx = k + ix
                var dLim = dx + iw
                if (k + width < dLim) {
                    dLim = k + width
                }
                var sx = i * iw
                while (dx < dLim) {
                    val index = pixels[sx++].toInt() and 0xff
                    val c = act[index]
                    if (c != 0) dest[dx] = c
                    dx++
                }
            }
        }
    }

    fun getFrame(n: Int): BufferedImage? = if (n in 0..<frameCount) frames[n].image else null

    fun getFrameSize(): Dimension = Dimension(width, height)

    fun read(inp: BufferedInputStream?): Int {
        init()
        if (inp != null) {
            input = inp
            readHeader()
            if (!err()) {
                readContents()
                if (frameCount < 0) {
                    status = STATUS_FORMAT_ERROR
                }
            } else {
                status = STATUS_OPEN_ERROR
            }
            inp.close()
        } else {
            status = STATUS_OPEN_ERROR
        }
        return status
    }

    fun read(inp: InputStream?): Int {
        init()
        if (inp != null) {
            input = if (inp !is BufferedInputStream) BufferedInputStream(inp) else inp
            readHeader()
            if (!err()) {
                readContents()
                if (frameCount < 0) {
                    status = STATUS_FORMAT_ERROR
                }
            } else {
                status = STATUS_OPEN_ERROR
            }
            inp.close()
        } else {
            status = STATUS_OPEN_ERROR
        }
        return status
    }

    fun read(name: String): Int {
        status = STATUS_OK
        try {
            val bufferedInputStream = BufferedInputStream(
                if (name.startsWith("file:") || name.contains(":/"))
                    URL(name).openStream()
                else
                    FileInputStream(File(name))
            )
            status = read(bufferedInputStream)
            bufferedInputStream.close()
        } catch (e: IOException) {
            status = STATUS_OPEN_ERROR
        }
        return status
    }

    private fun decodeImageData() {
        val nullCode = -1
        val numPix = iw * ih
        var available: Int
        val clear: Int
        var codeMask: Int
        var codeSize: Int
        var inCode: Int
        var oldCode: Int
        var code: Int
        var count: Int
        var datum: Int
        var first: Int
        var bi: Int
        if (pixels.size < numPix) {
            pixels = ByteArray(numPix)
        }
        if (prefix.isEmpty()) prefix = ShortArray(maxStackSize)
        if (suffix.isEmpty()) suffix = ByteArray(maxStackSize)
        if (pixelStack.isEmpty()) pixelStack = ByteArray(maxStackSize + 1)
        val dataSize: Int = read()
        clear = 1 shl dataSize
        val endOfInformation: Int = clear + 1
        available = clear + 2
        oldCode = nullCode
        codeSize = dataSize + 1
        codeMask = (1 shl codeSize) - 1
        for (c in 0 until clear) {
            prefix[c] = 0.toShort()
            suffix[c] = c.toByte()
        }
        datum = 0
        var bits = 0
        count = 0
        first = 0
        var top = 0
        var pi = 0
        bi = 0
        var i = 0
        while (i in 0 until numPix) {
            if (top == 0) {
                if (bits < codeSize) {
                    if (count == 0) {
                        count = readBlock()
                        if (count <= 0) break
                        bi = 0
                    }
                    datum += (block[bi].toInt() and 0xff) shl bits
                    bits += 8
                    bi++
                    count--
                    continue
                }
                code = datum and codeMask
                datum = datum shr codeSize
                bits -= codeSize
                if (code > available || code == endOfInformation) break
                if (code == clear) {
                    codeSize = dataSize + 1
                    codeMask = (1 shl codeSize) - 1
                    available = clear + 2
                    oldCode = nullCode
                    continue
                }
                if (oldCode == nullCode) {
                    pixelStack[top++] = suffix[code]
                    oldCode = code
                    first = code
                    continue
                }
                inCode = code
                if (code == available) {
                    pixelStack[top++] = first.toByte()
                    code = oldCode
                }
                while (code > clear) {
                    pixelStack[top++] = suffix[code]
                    code = prefix[code].toInt()
                }
                first = suffix[code].toInt() and 0xff
                if (available >= maxStackSize) {
                    pixelStack[top++] = first.toByte()
                    continue
                }
                pixelStack[top++] = first.toByte()
                prefix[available] = oldCode.toShort()
                suffix[available] = first.toByte()
                available++
                if ((available and codeMask) == 0 && available < maxStackSize) {
                    codeSize++
                    codeMask += available
                }
                oldCode = inCode
            }
            top--
            pixels[pi++] = pixelStack[top]
            i++
        }
        for (ii in pi until numPix) pixels[ii] = 0
    }

    private fun err(): Boolean = status != STATUS_OK

    private fun init() {
        status = STATUS_OK
        frameCount = 0
        frames = arrayListOf()
        gct = intArrayOf()
        lct = intArrayOf()
    }

    private fun read(): Int {
        val curByte = try {
            input!!.read()
        } catch (e: IOException) {
            status = STATUS_FORMAT_ERROR
            -1
        }
        return curByte
    }

    private fun readBlock(): Int {
        val blockSize = read()
        var n = 0
        if (blockSize > 0) {
            try {
                var count: Int
                while (n < blockSize) {
                    count = input!!.read(block, n, blockSize - n)
                    if (count == -1) break
                    n += count
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (n < blockSize) {
                status = STATUS_FORMAT_ERROR
            }
        }
        return n
    }

    private fun readColorTable(colors: Int): IntArray {
        val bytes = 3 * colors
        var tab: IntArray? = null
        val c = ByteArray(bytes)
        var n = 0
        try {
            n = input!!.read(c)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (n < bytes) {
            status = STATUS_FORMAT_ERROR
        } else {
            tab = IntArray(256)
            var i = 0
            var j = 0
            while (i < colors) {
                val r = c[j++].toInt() and 0xff
                val g = c[j++].toInt() and 0xff
                val b = c[j++].toInt() and 0xff
                tab[i++] = 0xff000000.toInt() or (r shl 16) or (g shl 8) or b
            }
        }
        return tab ?: intArrayOf()
    }

    private fun readContents() {
        var done = false
        while (!(done || err())) {
            when (read()) {
                0x2C -> readImage()
                0x21 -> {
                    when (read()) {
                        0xf9 -> readGraphicControlExt()
                        0xff -> {
                            readBlock()
                            val app = StringBuilder()
                            for (i in 0..10) {
                                app.append(Char(block[i].toInt()))
                            }
                            if (app.toString() == "NETSCAPE2.0") {
                                readNetscapeExt()
                            } else {
                                skip()
                            }
                        }

                        else -> skip()
                    }
                }

                0x3b -> done = true
                0x00 -> {}
                else -> status = STATUS_FORMAT_ERROR
            }
        }
    }

    private fun readGraphicControlExt() {
        read()
        val packed = read()
        dispose = (packed and 0x1c) shr 2
        if (dispose == 0) {
            dispose = 1
        }
        transparency = (packed and 1) != 0
        delay = readShort() * 10
        transIndex = read()
        read()
    }

    private fun readHeader() {
        val id = StringBuilder()
        for (i in 0 until 6) {
            id.append(read().toChar())
        }
        if (!id.toString().startsWith("GIF")) {
            status = STATUS_FORMAT_ERROR
            return
        }
        readLSD()
        if (gctFlag && !err()) {
            gct = readColorTable(gctSize)
            bgColor = gct[bgIndex]
        }
    }

    private fun readImage() {
        ix = readShort()
        iy = readShort()
        iw = readShort()
        ih = readShort()
        val packed = read()
        lctFlag = (packed and 0x80) != 0
        interlace = (packed and 0x40) != 0
        lctSize = 2 shl (packed and 7)
        if (lctFlag) {
            lct = readColorTable(lctSize)
            act = lct
        } else {
            act = gct
            if (bgIndex == transIndex) {
                bgColor = 0
            }
        }
        var save = 0
        if (act.isEmpty()) {
            status = STATUS_FORMAT_ERROR
            return
        }
        if (transparency) {
            save = act[transIndex]
            act[transIndex] = 0
        }
        if (err()) return
        decodeImageData()
        skip()
        if (err()) return
        frameCount++
        image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE)
        setPixels()
        frames.add(GifFrame(image, delay))
        if (transparency) {
            act[transIndex] = save
        }
        resetFrame()
    }

    private fun readLSD() {
        width = readShort()
        height = readShort()
        val packed = read()
        gctFlag = (packed and 0x80) != 0
        gctSize = 2 shl (packed and 7)
        bgIndex = read()
        pixelAspect = read()
    }

    private fun readNetscapeExt() {
        do {
            readBlock()
            if (block[0] == 1.toByte()) {
                val b1 = block[1].toInt() and 0xff
                val b2 = block[2].toInt() and 0xff
                loopCount = (b2 shl 8) or b1
            }
        } while (blockSize > 0 && !err())
    }

    private fun readShort(): Int = read() or (read() shl 8)

    private fun resetFrame() {
        lastDispose = dispose
        lastRect = Rectangle(ix, iy, iw, ih)
        lastImage = image
        lastBgColor = bgColor
        dispose = 0
        transparency = false
        delay = 0
        lct = intArrayOf()
    }

    private fun skip() {
        do {
            readBlock()
        } while (blockSize > 0 && !err())
    }
}
