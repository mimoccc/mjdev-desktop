package org.mjdev.desktop.extensions

import com.google.gson.GsonBuilder

object JSonExt {
    val gson = GsonBuilder().create()

    fun <T : Any> T.toJson() =
        gson.toJson(this)

    inline fun <reified T : Any> String.fromJson(): T =
        gson.fromJson(this, T::class.java)
}