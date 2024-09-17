package eu.mjdev.desktop.components.controlcenter

interface IControlCenterPageDataSaver {
    fun save(data: Map<Int, Any>)
    fun load(): Map<Int, Any>
}