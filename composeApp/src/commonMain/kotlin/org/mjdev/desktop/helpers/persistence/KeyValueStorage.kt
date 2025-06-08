package org.mjdev.desktop.helpers.persistence

interface KeyValueStorage {
    fun put(key: String, value: String)
    fun get(key: String, default: String? = null): String?
    fun remove(key: String)
    fun clear()
}