package org.mjdev.desktop.data;

enum class DesktopEntryType(val text: String) {
    Unknown(""),
    DesktopEntry("Desktop Entry"),
    Application("Application"),
    Theme("X-GNOME-Metatheme");

    companion object {
        operator fun invoke(
            value: String?
        ): DesktopEntryType = value.orEmpty().trim().let { v ->
            entries.firstOrNull { e -> e.text.contentEquals(v, true) } ?: Unknown
        }
    }
}