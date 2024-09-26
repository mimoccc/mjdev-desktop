package eu.mjdev.desktop.components.controlcenter.base

import androidx.compose.runtime.DisallowComposableCalls

@Suppress("unused")
class PageCache(
    private val saver: IControlCenterPageDataSaver? = null
) : HashMap<Int, Any>() {
    init {
        saver?.load()?.onEach { (t, u) -> this[t] = u }
    }

    fun save() {
        saver?.save(this)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> cache(
        invalid: Boolean,
        block: @DisallowComposableCalls () -> T
    ): T {
        val hash = block.hashCode()
        var data: T = this[hash] as T
        if (invalid || data == null) {
            data = block()
        }
        if (data != null) this[hash] = data
        return data
    }
}