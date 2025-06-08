package org.mjdev.desktop.helpers.persistence

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object StorageProvider {
    val instance: KeyValueStorage
}