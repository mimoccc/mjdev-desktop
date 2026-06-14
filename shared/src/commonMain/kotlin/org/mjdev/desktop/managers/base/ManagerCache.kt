package org.mjdev.desktop.managers.base

import org.mjdev.desktop.context.IDesktopContext
import kotlin.reflect.KClass

class ManagerCache(
    val context: IDesktopContext,
) {
    val managersCache = mutableMapOf<KClass<*>, Any?>()

    inline fun <reified T : IDelegate> get(): T {
        val type = T::class
        var manager = if (managersCache.containsKey(type)) managersCache[type] else null
        if (manager == null) {
            manager = context.createManager(type)
            if (manager == null) {
                throw (RuntimeException("No manager for ${T::class.simpleName} found."))
            } else {
                managersCache[type] = manager
            }
        }
        return managersCache[type] as T
    }
}
