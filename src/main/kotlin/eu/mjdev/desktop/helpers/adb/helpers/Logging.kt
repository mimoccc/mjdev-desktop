package eu.mjdev.desktop.helpers.adb.helpers

// todo
private const val ENABLED = true

internal fun log(block: () -> String) {
    if (ENABLED) {
        println(block())
    }
}
