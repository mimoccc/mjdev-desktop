package eu.mjdev.desktop.windows

class ComponentUpdater {
    private var updatedValues = mutableListOf<Any?>()

    fun update(body: UpdateScope.() -> Unit) {
        UpdateScope().body()
    }

    inner class UpdateScope {
        private var index = 0

        /**
         * Compare [value] with the old one and if it is changed - store a new value and call
         * [update]
         */
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