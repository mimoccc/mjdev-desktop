package org.psippr.desktop.helpers.persistence

import org.psippr.desktop.context.IDesktopContext

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect open class StorageProvider(
    context: IDesktopContext,
) {
    open fun put(
        key: String,
        value: String,
    )

    open fun get(
        key: String,
        default: String? = null,
    ): String?

    open fun getAll(): Map<String, String>

    open fun remove(key: String)

    open fun clear()
}
