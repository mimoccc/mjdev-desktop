package org.mjdev.desktop.extensions

import com.google.gson.GsonBuilder

object JSonExt {
    val gson = GsonBuilder()
        .disableInnerClassSerialization()
        .disableJdkUnsafe()
        .setLenient()
        .setPrettyPrinting()
        .create()

    fun <T : Any> T.toJson(): String =
        gson.toJson(this)

    inline fun <reified T : Any> String.fromJson(): T =
        gson.fromJson(this, T::class.java)
}