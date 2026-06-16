/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.helpers.compositor

import androidx.compose.ui.graphics.Color
import org.mjdev.desktop.log.Log
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * One-shot control commands to the mjdev compositor over its IPC socket.
 *
 * The desktop shell runs inside mjdevc; the compositor draws the server-side window
 * frames and colorizes them from the theme the shell sends here. When no compositor
 * socket is present (running outside mjdevc) every call is a silent no-op, so this is
 * safe to call unconditionally.
 */
object CompositorControl {
    private fun socketPath(): Path? {
        val dir = System.getenv("XDG_RUNTIME_DIR") ?: return null
        val path = Path.of(dir, "mjdev-compositor.sock")
        return if (Files.exists(path)) path else null
    }

    private fun send(line: String) {
        val path = socketPath() ?: return
        runCatching {
            SocketChannel.open(StandardProtocolFamily.UNIX).use { channel ->
                channel.connect(UnixDomainSocketAddress.of(path))
                channel.write(ByteBuffer.wrap((line + "\n").toByteArray(StandardCharsets.UTF_8)))
            }
        }.onFailure { e -> Log.w("CompositorControl: send failed: ${e.message}") }
    }

    private fun chan(v: Float) = (v.coerceIn(0f, 1f) * 255f).toInt()

    /**
     * Pushes the window-frame palette to the compositor — same roles as dock / control center:
     * [bg]/[fg] = backgroundColor / textColor; [iconBg]/[iconFg] = iconsTintColor / borderColor.
     */
    fun setDecorationTheme(
        bg: Color,
        fg: Color,
        iconBg: Color,
        iconFg: Color,
    ) {
        send(
            """{"cmd":"set-decoration-theme",""" +
                """"bg_r":${chan(bg.red)},"bg_g":${chan(bg.green)},"bg_b":${chan(bg.blue)},""" +
                """"fg_r":${chan(fg.red)},"fg_g":${chan(fg.green)},"fg_b":${chan(fg.blue)},""" +
                """"icon_bg_r":${chan(iconBg.red)},"icon_bg_g":${chan(iconBg.green)},""" +
                """"icon_bg_b":${chan(iconBg.blue)},""" +
                """"icon_fg_r":${chan(iconFg.red)},"icon_fg_g":${chan(iconFg.green)},""" +
                """"icon_fg_b":${chan(iconFg.blue)}}""",
        )
    }
}
