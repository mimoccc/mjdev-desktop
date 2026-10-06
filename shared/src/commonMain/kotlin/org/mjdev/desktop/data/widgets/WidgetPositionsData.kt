package org.mjdev.desktop.data.widgets

/**
 * Persisted form of all widget positions (the JSON file content).
 *
 * @property positions widget key (see [WidgetKey]) to its saved position
 */
data class WidgetPositionsData(
    var positions: MutableMap<String, WidgetPosition> = mutableMapOf(),
)
