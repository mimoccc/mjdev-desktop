package eu.mjdev.desktop.helpers.exception

@Suppress("unused")
class SuccessException(
    message: String = "-",
    e: Throwable? = null
) : Exception(message, e) {
    companion object {
        fun success(message: String) = SuccessException(message)
        fun success(e: Throwable) = SuccessException(e = e)
        fun success(message: String, e: Throwable) = SuccessException(message, e)
    }
}