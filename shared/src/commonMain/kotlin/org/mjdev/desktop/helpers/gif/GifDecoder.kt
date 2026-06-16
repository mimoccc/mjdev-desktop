/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.helpers.gif

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntRect
import okio.BufferedSource
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.source
import org.mjdev.desktop.extensions.imageBitmapFromArgb
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.system.Filesystem
import org.mjdev.desktop.system.Filesystem.source

// todo optimize code, remove unused variables, use better data structures
@Suppress("MemberVisibilityCanBePrivate", "unused")
class GifDecoder {
    private var status: Int = STATUS_OK
    private var width: Int = 0
    private var height: Int = 0
    private var globalColorTableSize: Int = 0
    private var loopCount: Int = 1
    private var backgroundIndex: Int = 0
    private var backgroundColor: Int = 0
    private var lastBackgroundColor: Int = 0
    private var pixelAspect: Int = 0
    private var localColorTableSize: Int = 0
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
    private var localColorTableFlag: Boolean = false
    private var interlace: Boolean = false
    private var globalColorTableFlag: Boolean = false
    private var prefix: ShortArray = shortArrayOf()
    private var suffix: ByteArray = byteArrayOf()
    private var pixelStack: ByteArray = byteArrayOf()
    private var pixels: ByteArray = byteArrayOf()
    private var lastRect: IntRect = IntRect.Zero
    private var globalColorTable: IntArray = intArrayOf()
    private var localColorTable: IntArray = intArrayOf()
    private var activeColorTable: IntArray = intArrayOf()
    private var input: BufferedSource? = null
    private var block = byteArrayOf()
    private var frameCount: Int = 0

    private val frames = mutableListOf<GifFrame>()

    // Compositing history (used only during decode): previous and two-frames-ago composited
    // canvases needed for the GIF dispose methods — bounded, not the whole movie.
    private var lastFramePixels: IntArray? = null
    private var lastLastFramePixels: IntArray? = null

    // Directory holding this gif's on-disk frame cache (raw ARGB per frame).
    private var cacheDir: Path? = null

    // Small in-memory LRU of frames built from disk — bounds live ImageBitmaps / GPU textures
    // to MAX_CACHED_BITMAPS instead of holding every frame of the animation in RAM.
    private val frameBitmapCache = mutableMapOf<Int, ImageBitmap>()
    private val frameCacheOrder = ArrayDeque<Int>()

    fun getWidth(): Int = width

    fun getHeight(): Int = height

    fun getDuration(): Long =
        frames
            .sumOf { f ->
                f.delay
            }.times(loopCount)
            .toLong()

    fun getDelay(n: Int): Long {
        delay = -1
        if (n in 0..<frameCount) {
            delay = frames[n].delay
        }
        return delay.toLong()
    }

    fun getFrameCount(): Int = frameCount

    fun getFrame(n: Int): ImageBitmap? {
        if (n !in 0..<frameCount) return null
        frameBitmapCache[n]?.let { cached ->
            frameCacheOrder.remove(n)
            frameCacheOrder.addLast(n)
            return cached
        }
        val frame = frames.getOrNull(n) ?: return null
        val bitmap =
            runCatching { imageBitmapFromArgb(readFramePixels(frame.file), width, height) }
                .onFailure { e -> Log.e(e) }
                .getOrNull() ?: return null
        frameBitmapCache[n] = bitmap
        frameCacheOrder.addLast(n)
        while (frameCacheOrder.size > MAX_CACHED_BITMAPS) {
            frameBitmapCache.remove(frameCacheOrder.removeFirst())
        }
        return bitmap
    }

    fun fromSource(inp: BufferedSource?): Int {
        init()
        // Frames are always streamed to disk; when no stable cache dir was set (e.g. a direct
        // fromSource/fromPath call), use an ephemeral one so we still avoid holding frames in RAM.
        if (cacheDir == null) {
            cacheDir = gifCacheBaseDir().resolve("tmp-${hashCode().toUInt().toString(16)}")
        }
        runCatching { Filesystem.createDirectories(cacheDir!!) }.onFailure { e -> Log.e(e) }
        if (inp != null) {
            input = inp
            readHeader()
            if (!isErr()) {
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

    fun fromFile(path: String): Int = fromPath(path.toPath())

    fun fromPath(path: Path): Int {
        status = STATUS_OK
        try {
            status = fromSource(source(path).buffer())
        } catch (e: IOException) {
            Log.e(e)
            status = STATUS_OPEN_ERROR
        }
        return status
    }

    fun fromUrl(url: String): Int =
        try {
            val connection = java.net.URL(url).openConnection()
            connection.connect()
            fromSource(connection.getInputStream().source().buffer())
        } catch (e: Exception) {
            Log.e(e)
            status = STATUS_OPEN_ERROR
            status
        }

    fun from(pathOrUrl: String): Int {
        init()
        val dir = stableCacheDir(pathOrUrl)
        cacheDir = dir
        runCatching { Filesystem.createDirectories(dir) }.onFailure { e -> Log.e(e) }
        // Persistent cache hit: frames already on disk from a previous run — skip decoding entirely.
        if (loadManifest(dir, pathOrUrl)) {
            status = STATUS_OK
            return status
        }
        status =
            when {
                pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://") -> {
                    fromUrl(pathOrUrl)
                }
                else -> {
                    fromFile(pathOrUrl)
                }
            }
        if (status == STATUS_OK) {
            runCatching { saveManifest(dir, pathOrUrl) }.onFailure { e -> Log.e(e) }
        }
        return status
    }

    // Composites the current frame onto the right base canvas (per GIF dispose method) and returns
    // the full-canvas ARGB pixels. Output identical to the old setPixels; it just works on plain
    // IntArray history buffers instead of reading pixels back out of ImageBitmaps.
    private fun compositeCurrentFrame(): IntArray {
        val dest = IntArray(width * height)
        // dispose of the PREVIOUS frame decides the base: 3 = restore to two-frames-ago,
        // 1/2 = keep previous, 0 = none (first frame). (dispose==2's fillRect was a no-op before.)
        val base =
            when {
                lastDispose == 3 -> lastLastFramePixels
                lastDispose > 0 -> lastFramePixels
                else -> null
            }
        if (base != null && base.size >= width * height) {
            System.arraycopy(base, 0, dest, 0, width * height)
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
                    val c = activeColorTable[index]
                    if (c != 0) dest[dx] = c
                    dx++
                }
            }
        }
        return dest
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
        val dataSize: Int = readByte()
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

    private fun isErr(): Boolean = status != STATUS_OK

    private fun init() {
        status = STATUS_OK
        frameCount = 0
        frames.clear()
        globalColorTable = intArrayOf()
        localColorTable = intArrayOf()
        lastFramePixels = null
        lastLastFramePixels = null
        frameBitmapCache.clear()
        frameCacheOrder.clear()
    }

    // --- on-disk frame cache -------------------------------------------------------------------

    // Cache key from source + file size + mtime so it invalidates when the gif changes.
    private fun stableCacheDir(src: String): Path {
        val meta = runCatching { Filesystem.metadataOrNull(src.toPath()) }.getOrNull()
        val size = meta?.size ?: -1L
        val mtime = meta?.lastModifiedAtMillis ?: -1L
        val key = "$src|$size|$mtime".hashCode().toUInt().toString(16)
        return gifCacheBaseDir().resolve(key)
    }

    private fun writeFramePixels(
        file: Path,
        pixels: IntArray,
    ) {
        val bytes = ByteArray(pixels.size * 4)
        for (i in pixels.indices) {
            val v = pixels[i]
            val o = i * 4
            bytes[o] = (v ushr 24).toByte()
            bytes[o + 1] = (v ushr 16).toByte()
            bytes[o + 2] = (v ushr 8).toByte()
            bytes[o + 3] = v.toByte()
        }
        Filesystem.sink(file).buffer().use { sink -> sink.write(bytes) }
    }

    private fun readFramePixels(file: Path): IntArray {
        val bytes = Filesystem.source(file).buffer().use { it.readByteArray() }
        val out = IntArray(bytes.size / 4)
        for (i in out.indices) {
            val o = i * 4
            out[i] =
                ((bytes[o].toInt() and 0xff) shl 24) or
                ((bytes[o + 1].toInt() and 0xff) shl 16) or
                ((bytes[o + 2].toInt() and 0xff) shl 8) or
                (bytes[o + 3].toInt() and 0xff)
        }
        return out
    }

    private fun saveManifest(
        dir: Path,
        src: String,
    ) {
        val text =
            buildString {
                appendLine("v1")
                appendLine(src)
                appendLine(width.toString())
                appendLine(height.toString())
                appendLine(loopCount.toString())
                appendLine(frameCount.toString())
                appendLine(frames.joinToString(",") { it.delay.toString() })
            }
        Filesystem.writeText(dir.resolve("info"), text)
    }

    // Populates frames from a previously written manifest (no decoding). Returns false on any
    // mismatch / missing file so the caller re-decodes.
    private fun loadManifest(
        dir: Path,
        src: String,
    ): Boolean =
        runCatching {
            val info = dir.resolve("info")
            if (!Filesystem.fileExists(info)) return false
            val lines = Filesystem.readLines(info)
            if (lines.size < 7 || lines[0] != "v1" || lines[1] != src) return false
            val w = lines[2].toInt()
            val h = lines[3].toInt()
            val loop = lines[4].toInt()
            val count = lines[5].toInt()
            val delays = lines[6].split(",").mapNotNull { it.toIntOrNull() }
            if (count <= 0 || delays.size != count) return false
            val restored = mutableListOf<GifFrame>()
            for (i in 0 until count) {
                val f = dir.resolve("f$i")
                if (!Filesystem.fileExists(f)) return false
                restored.add(GifFrame(delays[i], f))
            }
            width = w
            height = h
            loopCount = loop
            frameCount = count
            frames.clear()
            frames.addAll(restored)
            true
        }.getOrDefault(false)

    private fun readByte(): Int {
        val curByte =
            try {
                input!!.readByte()
            } catch (e: IOException) {
                Log.e(e)
                status = STATUS_FORMAT_ERROR
                -1
            }
        return curByte.toInt() and 0xff
    }

    private fun readBlock(): Int {
        val blockSize = readByte()
        if (blockSize > 0) {
            try {
                block = input!!.readByteArray(blockSize.toLong())
            } catch (e: IOException) {
                Log.e(e)
                status = STATUS_FORMAT_ERROR
            }
        }
        return blockSize
    }

    private fun readColorTable(colors: Int): IntArray {
        var tab: IntArray
        try {
            val bytes = 3 * colors
            val c = input!!.readByteArray(bytes.toLong())
            if (c.size < bytes) {
                tab = intArrayOf()
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
        } catch (e: IOException) {
            Log.e(e)
            tab = intArrayOf()
            status = STATUS_FORMAT_ERROR
        }
        return tab
    }

    private fun readContents() {
        var done = false
        while (!(done || isErr())) {
            val token = readByte()
            when (token) {
                0x2C -> readImage()
                0x21 -> {
                    when (readByte()) {
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
        readByte()
        val packed = readByte()
        dispose = (packed and 0x1c) shr 2
        if (dispose == 0) {
            dispose = 1
        }
        transparency = (packed and 1) != 0
        delay = readShort() * 10
        transIndex = readByte()
        readByte()
    }

    private fun readHeader() {
        val id = StringBuilder()
        for (i in 0 until 6) {
            id.append(readByte().toChar())
        }
        if (!id.toString().startsWith("GIF")) {
            status = STATUS_FORMAT_ERROR
            return
        }
        readLSD()
        if (globalColorTableFlag && !isErr()) {
            globalColorTable = readColorTable(globalColorTableSize)
            backgroundColor = globalColorTable[backgroundIndex]
        }
    }

    private fun readImage() {
        ix = readShort()
        iy = readShort()
        iw = readShort()
        ih = readShort()
        val packed = readByte()
        localColorTableFlag = (packed and 0x80) != 0
        interlace = (packed and 0x40) != 0
        localColorTableSize = 2 shl (packed and 7)
        if (localColorTableFlag) {
            localColorTable = readColorTable(localColorTableSize)
            activeColorTable = localColorTable
        } else {
            activeColorTable = globalColorTable
            if (backgroundIndex == transIndex) {
                backgroundColor = 0
            }
        }
        var save = 0
        if (activeColorTable.isEmpty()) {
            status = STATUS_FORMAT_ERROR
            return
        }
        if (transparency) {
            save = activeColorTable[transIndex]
            activeColorTable[transIndex] = 0
        }
        if (isErr()) return
        decodeImageData()
        skip()
        if (isErr()) return
        frameCount++
        val dest = compositeCurrentFrame()
        val index = frameCount - 1
        val frameFile = cacheDir!!.resolve("f$index")
        runCatching { writeFramePixels(frameFile, dest) }.onFailure { e -> Log.e(e) }
        frames.add(GifFrame(delay, frameFile))
        if (transparency) {
            activeColorTable[transIndex] = save
        }
        // advance compositing history for the next frame's dispose handling
        lastLastFramePixels = lastFramePixels
        lastFramePixels = dest
        resetFrame()
    }

    private fun readLSD() {
        width = readShort()
        height = readShort()
        val packed = readByte()
        globalColorTableFlag = (packed and 0x80) != 0
        globalColorTableSize = 2 shl (packed and 7)
        backgroundIndex = readByte()
        pixelAspect = readByte()
    }

    private fun readNetscapeExt() {
        do {
            readBlock()
            if (block[0] == 1.toByte()) {
                val b1 = block[1].toInt() and 0xff
                val b2 = block[2].toInt() and 0xff
                loopCount = (b2 shl 8) or b1
            }
        } while (blockSize > 0 && !isErr())
    }

    private fun readShort(): Int = (readByte() or (readByte() shl 8)) and 0xffff

    private fun resetFrame() {
        lastDispose = dispose
        lastRect = IntRect(ix, iy, iw, ih)
        lastBackgroundColor = backgroundColor
        dispose = 0
        transparency = false
        delay = 0
        localColorTable = intArrayOf()
    }

    private fun skip() {
        do {
            readBlock()
        } while (blockSize > 0 && !isErr())
    }

    companion object {
        const val STATUS_OK = 0
        const val STATUS_FORMAT_ERROR = 1
        const val STATUS_OPEN_ERROR = 2

        // Max frames kept as live ImageBitmaps in RAM at once (rest stream from disk on demand).
        const val MAX_CACHED_BITMAPS = 4

        fun fromSource(source: BufferedSource?): GifDecoder =
            GifDecoder().apply {
                fromSource(source)
            }

        fun fromPath(source: Path): GifDecoder =
            GifDecoder().apply {
                fromPath(source)
            }

        fun fromUrl(source: String): GifDecoder =
            GifDecoder().apply {
                fromUrl(source)
            }

        fun fromPathOrUrl(source: String): GifDecoder =
            GifDecoder().apply {
                fromPathOrUrl(source)
            }

        fun fromFile(source: String): GifDecoder =
            GifDecoder().apply {
                fromFile(source)
            }
    }
}
