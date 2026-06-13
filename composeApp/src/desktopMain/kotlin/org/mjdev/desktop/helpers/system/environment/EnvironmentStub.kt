package org.mjdev.desktop.helpers.system.environment

open class EnvironmentStub(
    getenv: () -> Map<String, String> = { mapOf() },
) {
    protected val data = mutableMapOf<String, String>()

    init {
        data.putAll(getenv())
    }

    fun toMap(): Map<String, String> = data

    fun toTypedArray() = data.map { p -> "${p.key}=${p.value}" }.toTypedArray()
}
