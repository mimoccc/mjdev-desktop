package org.mjdev.desktop.interfaces

interface IControlCenterPageDataSaver {
    val key:String

    fun save(data: Map<Int, Any>)
    fun load(): Map<Int, Any>
}