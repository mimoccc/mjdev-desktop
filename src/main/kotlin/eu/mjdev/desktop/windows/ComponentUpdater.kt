package eu.mjdev.desktop.windows

internal class ComponentUpdater {
    private var updatedValues = mutableListOf<Any?>()

    fun update(body: UpdateScope.() -> Unit) {
        UpdateScope().body()
    }

    inner class UpdateScope {
        private var index = 0

        fun <T : Any?> set(value: T, update: (T) -> Unit) {
            if (index < updatedValues.size) {
                if (updatedValues[index] != value) {
                    update(value)
                    updatedValues[index] = value
                }
            } else {
                check(index == updatedValues.size)
                update(value)
                updatedValues.add(value)
            }
            index++
        }
    }
}