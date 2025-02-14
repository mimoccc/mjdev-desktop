package org.mjdev.desktop.interfaces

interface IControlCenterPageDataSaver {
    fun save(data: Map<Int, Any>)
    fun load(): Map<Int, Any>
}