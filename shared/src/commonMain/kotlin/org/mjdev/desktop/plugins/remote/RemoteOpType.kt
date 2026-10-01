package org.mjdev.desktop.plugins.remote

/** Drawing operations a [RemoteDocument] can contain. */
enum class RemoteOpType {
    /** Filled or stroked rectangle. */
    RECT,

    /** Arc (ring segment) centered in the op bounds, used for donut charts and gauges. */
    ARC,

    /** Text line, may contain `{name}` placeholders resolved from [RemoteVariables.texts]. */
    TEXT,
    ;

    companion object {
        /** Parses [value] ignoring case, or returns null for unknown operations. */
        fun parse(value: String): RemoteOpType? = entries.firstOrNull { it.name.equals(value, true) }
    }
}
