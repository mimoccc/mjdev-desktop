package org.mjdev.desktop.helpers.system.shell

import org.mjdev.desktop.data.App

class ShellScope(
    val shell: Shell
) {
    suspend fun autoStartApps() = shell.autoStartApps()

    suspend fun startApp(
        app: App,
        onStarted: () -> Unit = {},
        onStopped: (e: Throwable) -> Unit = {},
    ) = shell.startApp(app, onStarted, onStopped)

    suspend fun executeAndRead(
        cmd: String,
        vararg args: String
    ): String = Shell.executeAndRead(cmd, *args)

    suspend fun executeAndReadLines(
        cmd: String,
        vararg args: String
    ): List<String> = Shell.executeAndReadLines(cmd, *args)
}