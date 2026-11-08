package org.psippr.desktop.helpers.system.environment

import java.io.File

/**
 * Gives apps started by the desktop the `GDK_BACKEND` the desktop itself was started with.
 *
 * Java's AWT GTK loader (`libawt_xawt.so`) sets `GDK_BACKEND=x11` inside the desktop process, and
 * every child inherited it: GTK apps (Nautilus) then ran through XWayland, where no XSETTINGS
 * daemon passes on the GNOME settings, so they ignored the generated icon theme and never
 * reloaded the palette colours.
 */
object InheritedGdkBackend {
    /** Variable choosing the GDK display backend. */
    const val GDK_BACKEND = "GDK_BACKEND"

    /** Separates the variables in `/proc/<pid>/environ`. */
    private const val ENTRY_SEPARATOR = '\u0000'

    /** Separates a variable's name from its value. */
    private const val VALUE_SEPARATOR = '='

    /** Environment this process was started with, before anything inside it changed it. */
    private val START_ENVIRONMENT = File("/proc/self/environ")

    /** Puts the start value of [GDK_BACKEND] into [env]; leaves [env] alone when it is unknown. */
    fun restore(env: MutableMap<String, String>) {
        startEnvironment()?.let { start -> restore(env, start) }
    }

    /** Puts [GDK_BACKEND] from [start] into [env], or removes it when [start] has none. */
    fun restore(
        env: MutableMap<String, String>,
        start: Map<String, String>,
    ) {
        start[GDK_BACKEND]?.let { value -> env[GDK_BACKEND] = value } ?: env.remove(GDK_BACKEND)
    }

    /** Start environment of this process, null where it cannot be read (not Linux). */
    fun startEnvironment(file: File = START_ENVIRONMENT): Map<String, String>? = runCatching {
        parse(file.readText())
    }.getOrNull()

    /** Variables of an `environ` text. */
    fun parse(environ: String): Map<String, String> = environ
        .split(ENTRY_SEPARATOR)
        .filter { entry -> VALUE_SEPARATOR in entry }
        .associate { entry -> entry.substringBefore(VALUE_SEPARATOR) to entry.substringAfter(VALUE_SEPARATOR) }
}
