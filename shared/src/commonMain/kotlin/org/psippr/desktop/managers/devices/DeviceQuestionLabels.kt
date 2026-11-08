package org.psippr.desktop.managers.devices

/** Texts of the notification that asks about a newly connected device. */
object DeviceQuestionLabels {
    const val TITLE = "New device"

    /** Message; the device name goes in front. */
    const val MESSAGE = " is connected. Connect it to the desktop?"
    const val CONNECT = "Connect"
    const val DISMISS = "Dismiss"
    const val REMEMBER = "Remember device"

    /** Action ids the answer carries. */
    const val ACTION_CONNECT = "connect"
    const val ACTION_DISMISS = "dismiss"
}
