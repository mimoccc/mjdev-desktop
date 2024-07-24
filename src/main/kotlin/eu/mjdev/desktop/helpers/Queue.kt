package eu.mjdev.desktop.helpers

@Suppress("unused")
class Queue<E> : ArrayList<E>() {
    private var index = 0

    val value: E?
        get() = runCatching {
            val ret = this[index] as E?
            if (index < size) index++
            if (index >= size) index = 0
            ret
        }.onFailure {
            index = 0
        }.getOrNull()

    companion object {
        fun <E> mutableQueue(vararg elements: E) = Queue<E>().apply { addAll(elements) }

        fun <E> mutableQueue(elements: Collection<E>) = Queue<E>().apply { addAll(elements) }
    }
}


