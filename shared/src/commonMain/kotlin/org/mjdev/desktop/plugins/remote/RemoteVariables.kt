package org.mjdev.desktop.plugins.remote

/** Live values a [RemoteDocument] binds to; produced by the plugin on every refresh. */
data class RemoteVariables(
    /** Numeric values by name, e.g. a ratio 0..1 that drives an arc sweep. */
    val numbers: Map<String, Float> = emptyMap(),
    /** Text values by name, substituted for `{name}` placeholders in text ops. */
    val texts: Map<String, String> = emptyMap(),
)
