package org.mjdev.desktop.components.aidesktop

enum class AiControlCenterTab(
    val title: String,
    val iconName: String,
) {
    System("System", "settings"),
    Sound("Sound", "sound"),
    Network("Network", "wifi"),
    Display("Display", "display"),
    Theme("Theme", "palette"),
    Ai("AI", "ai"),
}
