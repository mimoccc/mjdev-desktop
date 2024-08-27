package eu.mjdev.dadb.helpers

private val ENABLED = "true" == System.getenv("DADB_LOGGING")

internal fun log(block: () -> String) {
    if (ENABLED) {
        println(block())
    }
}
