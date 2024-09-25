package eu.mjdev.desktop.helpers.adb.forwarding

data class TcpForwardDescriptor(
    val resource: AutoCloseable,
    val localPort: Int
) : AutoCloseable {
    override fun close() {
        resource.close()
    }
}