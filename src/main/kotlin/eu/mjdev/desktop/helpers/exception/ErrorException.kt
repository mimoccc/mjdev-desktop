package eu.mjdev.desktop.helpers.exception

@Suppress("unused")
class ErrorException(
    message: String = "-",
    e: Throwable? = null
) : Exception(message, e) {
    companion object {
        fun error(message: String) = ErrorException(message)
        fun error(e: Throwable) = ErrorException(e = e)
        fun error(message: String, e: Throwable) = ErrorException(message, e)
    }
}