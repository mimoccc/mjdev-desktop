package org.mjdev.desktop.managers.remote

import org.mjdev.desktop.log.Log
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.event.InputEvent
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

/**
 * One connected RFB (VNC) client. Speaks RFB 3.8 with "None" security and serves the whole
 * screen as Raw-encoded 32bpp rectangles captured through [Robot]; pointer and key events from
 * the viewer are injected back through the same [Robot]. Raw encoding only — universally
 * supported by every viewer, at the cost of bandwidth (fine on a LAN).
 *
 * Blocking sockets on a dedicated thread (owned by [RemoteDesktopManager]); the run loop exits
 * when the socket closes or the server stops.
 */
class RfbClient(
    private val socket: Socket,
    private val robot: Robot,
) {
    private val input = DataInputStream(BufferedInputStream(socket.getInputStream()))
    private val output = DataOutputStream(socket.getOutputStream())

    private val screen = Toolkit.getDefaultToolkit().screenSize
    private val width = screen.width
    private val height = screen.height

    fun serve() {
        handshake()
        messageLoop()
    }

    fun close() = runCatching { socket.close() }

    // ---- RFB 3.8 handshake ------------------------------------------------
    private fun handshake() {
        output.writeBytes(PROTOCOL_VERSION)
        output.flush()
        val clientVersion = ByteArray(12).also { input.readFully(it) }
        Log.d("RfbClient: client version ${String(clientVersion).trim()}")

        // security: advertise only "None" (1)
        output.writeByte(1)
        output.writeByte(SECURITY_NONE)
        output.flush()
        val chosen = input.readUnsignedByte()
        if (chosen != SECURITY_NONE) throw IllegalStateException("unsupported security $chosen")
        output.writeInt(0) // SecurityResult: OK

        input.readUnsignedByte() // ClientInit shared-flag (ignored — we always allow sharing)
        writeServerInit()
    }

    private fun writeServerInit() {
        output.writeShort(width)
        output.writeShort(height)
        writePixelFormat()
        val name = SERVER_NAME.toByteArray()
        output.writeInt(name.size)
        output.write(name)
        output.flush()
    }

    // 32bpp true-colour BGRX (the most compatible fixed format)
    private fun writePixelFormat() {
        output.writeByte(32) // bits-per-pixel
        output.writeByte(24) // depth
        output.writeByte(0) // big-endian-flag
        output.writeByte(1) // true-colour-flag
        output.writeShort(255) // red-max
        output.writeShort(255) // green-max
        output.writeShort(255) // blue-max
        output.writeByte(16) // red-shift
        output.writeByte(8) // green-shift
        output.writeByte(0) // blue-shift
        output.write(ByteArray(3)) // padding
    }

    // ---- client -> server message loop ------------------------------------
    private fun messageLoop() {
        while (!socket.isClosed) {
            when (input.readUnsignedByte()) {
                MSG_SET_PIXEL_FORMAT -> skip(3 + 16)
                MSG_SET_ENCODINGS -> {
                    skip(1)
                    val count = input.readUnsignedShort()
                    skip(count * 4)
                }
                MSG_FRAMEBUFFER_UPDATE_REQUEST -> {
                    skip(1) // incremental flag — we always send the full rect (Raw, correct either way)
                    val x = input.readUnsignedShort()
                    val y = input.readUnsignedShort()
                    val w = input.readUnsignedShort()
                    val h = input.readUnsignedShort()
                    sendFramebuffer(x, y, w, h)
                }
                MSG_KEY_EVENT -> {
                    val down = input.readUnsignedByte() != 0
                    skip(2)
                    val keysym = input.readInt().toLong() and 0xFFFFFFFFL
                    injectKey(keysym, down)
                }
                MSG_POINTER_EVENT -> {
                    val buttonMask = input.readUnsignedByte()
                    val x = input.readUnsignedShort()
                    val y = input.readUnsignedShort()
                    injectPointer(buttonMask, x, y)
                }
                MSG_CLIENT_CUT_TEXT -> {
                    skip(3)
                    val len = input.readInt()
                    skip(len)
                }
                else -> return // unknown message: drop the client rather than desync the stream
            }
        }
    }

    private fun sendFramebuffer(
        x: Int,
        y: Int,
        w: Int,
        h: Int,
    ) {
        val rw = w.coerceAtMost(width - x).coerceAtLeast(0)
        val rh = h.coerceAtMost(height - y).coerceAtLeast(0)
        output.writeByte(MSG_FRAMEBUFFER_UPDATE)
        output.writeByte(0) // padding
        if (rw == 0 || rh == 0) {
            output.writeShort(0)
            output.flush()
            return
        }
        output.writeShort(1) // one rectangle
        output.writeShort(x)
        output.writeShort(y)
        output.writeShort(rw)
        output.writeShort(rh)
        output.writeInt(ENCODING_RAW)
        // Capture on THIS client thread, never the EDT: on a Wayland host Robot capture goes
        // through the (blocking) XDG screencast portal, and doing that on the EDT freezes the
        // whole Compose UI. On the client thread only this one viewer stalls until the portal
        // grants access (X11 sessions capture directly with no portal).
        val image = robot.createScreenCapture(Rectangle(x, y, rw, rh))
        val pixels = image.getRGB(0, 0, rw, rh, null, 0, rw)
        val bytes = ByteArray(rw * rh * 4)
        var b = 0
        for (argb in pixels) {
            bytes[b++] = (argb and 0xFF).toByte() // blue
            bytes[b++] = (argb ushr 8 and 0xFF).toByte() // green
            bytes[b++] = (argb ushr 16 and 0xFF).toByte() // red
            bytes[b++] = 0 // padding (x)
        }
        output.write(bytes)
        output.flush()
    }

    private fun injectKey(
        keysym: Long,
        down: Boolean,
    ) {
        val code = RfbKeymap.keyCode(keysym) ?: return
        runCatching {
            if (down) robot.keyPress(code) else robot.keyRelease(code)
        }.onFailure { e -> Log.w("RfbClient: key $keysym failed: ${e.message}") }
    }

    private var lastButtons = 0

    private fun injectPointer(
        buttonMask: Int,
        x: Int,
        y: Int,
    ) {
        robot.mouseMove(x, y)
        // bit0=left, bit1=middle, bit2=right; bit3/4 = wheel up/down
        updateButton(buttonMask, 0x1, InputEvent.BUTTON1_DOWN_MASK)
        updateButton(buttonMask, 0x2, InputEvent.BUTTON2_DOWN_MASK)
        updateButton(buttonMask, 0x4, InputEvent.BUTTON3_DOWN_MASK)
        if (buttonMask and 0x8 != 0) robot.mouseWheel(-1)
        if (buttonMask and 0x10 != 0) robot.mouseWheel(1)
        lastButtons = buttonMask
    }

    private fun updateButton(
        mask: Int,
        bit: Int,
        awtMask: Int,
    ) {
        val nowDown = mask and bit != 0
        val wasDown = lastButtons and bit != 0
        if (nowDown && !wasDown) robot.mousePress(awtMask)
        if (!nowDown && wasDown) robot.mouseRelease(awtMask)
    }

    private fun skip(n: Int) {
        var remaining = n
        while (remaining > 0) {
            val skipped = input.skip(remaining.toLong()).toInt()
            if (skipped <= 0) {
                input.readByte()
                remaining--
            } else {
                remaining -= skipped
            }
        }
    }

    companion object {
        private const val PROTOCOL_VERSION = "RFB 003.008\n"
        private const val SERVER_NAME = "mjdev-desktop"
        private const val SECURITY_NONE = 1
        private const val ENCODING_RAW = 0

        // client -> server message types
        private const val MSG_SET_PIXEL_FORMAT = 0
        private const val MSG_SET_ENCODINGS = 2
        private const val MSG_FRAMEBUFFER_UPDATE_REQUEST = 3
        private const val MSG_KEY_EVENT = 4
        private const val MSG_POINTER_EVENT = 5
        private const val MSG_CLIENT_CUT_TEXT = 6

        // server -> client message type
        private const val MSG_FRAMEBUFFER_UPDATE = 0
    }
}
