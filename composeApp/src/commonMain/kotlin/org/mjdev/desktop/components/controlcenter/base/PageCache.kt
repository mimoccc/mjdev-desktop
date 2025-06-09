package org.mjdev.desktop.components.controlcenter.base

import androidx.compose.runtime.DisallowComposableCalls
import org.mjdev.desktop.interfaces.IControlCenterPageDataSaver

class PageCache(
    private val saver: IControlCenterPageDataSaver? = null
) {
    private val _data: MutableMap<Int, Any> = mutableMapOf()

    fun load() {
        saver?.load()?.onEach { (t, u) ->
            _data[t] = u
        }
    }

    fun save() {
        saver?.save(_data)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> cache(
        invalid: Boolean,
        block: @DisallowComposableCalls () -> T
    ): T {
        val hash = block.hashCode()
        var data: T = _data[hash] as T
        if (invalid || data == null) {
            data = block()
        }
        if (data != null) _data[hash] = data
        return data
    }
}