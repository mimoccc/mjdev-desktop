package eu.mjdev.desktop.data

data class Category(
    val name: String,
    val priority: Int = parsePriority(name),
) {
    override fun toString(): String {
        return name
    }

    companion object {
        const val UNCATEGORIZED: String = "Uncategorized"

        val Empty: Category = Category(UNCATEGORIZED, -1)

        private fun parsePriority(name: String): Int =
            if (name.contentEquals(UNCATEGORIZED, true)) -1 else 1
    }
}
