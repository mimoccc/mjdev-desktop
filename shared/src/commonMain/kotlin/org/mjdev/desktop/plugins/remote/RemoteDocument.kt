package org.mjdev.desktop.plugins.remote

import org.mjdev.desktop.helpers.generic.JsonHelper
import org.mjdev.desktop.helpers.generic.JsonHelper.toJson

/**
 * A serializable widget layout in the spirit of Remote Compose: the UI is plain data (a list of
 * [RemoteOp]s) that the desktop renders with [RemoteDocumentPlayer], so it can be stored, sent
 * over the network or shipped inside a plugin jar and drawn without running plugin UI code.
 */
data class RemoteDocument(
    /** Format version, bumped on breaking changes. */
    val version: Int = FORMAT_VERSION,
    /** Preferred widget width in dp. */
    val width: Float = DEFAULT_WIDTH,
    /** Preferred widget height in dp. */
    val height: Float = DEFAULT_HEIGHT,
    /** Where the widget sits on the desktop, see [RemoteAlign]. */
    val anchor: String = RemoteAlign.BOTTOM_END.name,
    /** Drawing instructions, painted in order. */
    val ops: List<RemoteOp> = emptyList(),
) {
    /** Serializes this document to JSON. */
    fun encode(): String = toJson()

    companion object {
        /** Current document format version. */
        const val FORMAT_VERSION = 1

        /** Default widget width in dp. */
        const val DEFAULT_WIDTH = 350f

        /** Default widget height in dp. */
        const val DEFAULT_HEIGHT = 300f

        /** Parses a document from [json], or returns null when it is not valid. */
        fun decode(json: String): RemoteDocument? = runCatching {
            JsonHelper.fromJson<RemoteDocument>(json)
        }.getOrNull()
    }
}
