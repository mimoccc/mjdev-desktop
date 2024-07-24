package eu.mjdev.desktop.provider.data

data class Category(
    val name : String
) {
    override fun toString(): String {
        return name
    }

    companion object {
        const val UNCATEGORIZED: String = "Uncategorized"

        val Empty: Category = Category(UNCATEGORIZED)
    }
}
