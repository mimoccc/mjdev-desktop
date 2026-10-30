package org.psippr.desktop.components.controlcenter.pages.devices

/** Labels and timings of the devices tab. */
object DevicesPageDefaults {
    /** Name of the tab. */
    const val PAGE_NAME = "Connected devices"

    /** Pause between two reads of the device list (each read asks every device). */
    const val REFRESH_MS = 3_000L

    /** Section shown while no device is connected. */
    const val NONE_TITLE = "No devices connected"
    const val NONE_HINT =
        "Connect an Android phone or tablet with USB debugging switched on (Developer options), " +
            "then allow this computer on its screen."

    /** Shown while an action runs. */
    const val RUNNING = "Working…"

    /** Button text asking to confirm a restart. */
    const val CONFIRM = "Sure?"
}
