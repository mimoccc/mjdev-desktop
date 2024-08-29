package eu.mjdev.dadb.helpers

// todo
private val ENABLED = true

internal fun log(block: () -> String) {
    if (ENABLED) {
        println(block())
    }
}
