package org.psippr.desktop.helpers.exception

class EmptyException : Exception() {
    companion object {
        val EmptyException = EmptyException()
    }
}
