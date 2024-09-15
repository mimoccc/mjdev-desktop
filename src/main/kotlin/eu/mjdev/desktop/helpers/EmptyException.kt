package eu.mjdev.desktop.helpers

class EmptyException : Exception() {
    companion object {
        val EmptyException = EmptyException()
    }
}