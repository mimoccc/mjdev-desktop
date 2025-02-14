package org.mjdev.desktop.managers.base

import org.mjdev.desktop.interfaces.IDesktopContext
import kotlin.reflect.KClass

class ManagerCache(
    val context: IDesktopContext
) {
    val managersCache = mutableMapOf<KClass<*>, Any>()

    inline fun <reified T : IDelegate> get(): T {
        val type = T::class
        if (!managersCache.containsKey(type)) {
            val manager = context.createManager(type)
            if (manager == null) {
                throw (RuntimeException("No manager for ${T::class.simpleName} found."))
            } else {
                managersCache[type] = manager
            }
        }
        return managersCache[type] as T
    }
}