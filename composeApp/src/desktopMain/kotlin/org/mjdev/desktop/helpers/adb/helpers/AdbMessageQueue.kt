package org.mjdev.desktop.helpers.adb.helpers

internal class AdbMessageQueue(
    private val adbReader: AdbReader,
) : MessageQueue<AdbMessage>(),
    AutoCloseable {
    override fun readMessage() = adbReader.readMessage()

    override fun getLocalId(message: AdbMessage) = message.arg1

    override fun getCommand(message: AdbMessage) = message.command

    override fun close() = adbReader.close()

    override fun isCloseCommand(message: AdbMessage) = message.command == AdbConstants.CMD_CLSE
}
