package eu.mjdev.desktop.helpers.adb.helpers

// todo
private const val ENABLED = true

fun log(block: () -> String) {
    if (ENABLED) {
        println(block())
    }
}
