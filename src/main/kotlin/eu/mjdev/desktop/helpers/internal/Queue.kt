package eu.mjdev.desktop.helpers.internal

@Suppress("unused")
class Queue<E>(
    val source: Collection<E>
) : Iterator<E> {
    var iterator: Iterator<E> = source.iterator()

    override fun hasNext(): Boolean = source.isNotEmpty()

    @Deprecated(
        message = "Deprecated please use for safety nextOrNull().",
    )
    override fun next(): E = when {
        source.isEmpty() -> throw (IllegalStateException("Source is empty."))
        iterator.hasNext() -> iterator.next()
        else -> {
            iterator = source.iterator()
            iterator.next()
        }
    }

    fun nextOrNull(): E? = when {
        source.isEmpty() -> null
        iterator.hasNext() -> iterator.next()
        else -> {
            iterator = source.iterator()
            nextOrNull()
        }
    }
}
