package eu.mjdev.desktop.provider.data

import androidx.compose.ui.Alignment

@Suppress("unused")
enum class ControlCenterLocation(
    val alignment: Alignment
) {
    Left(Alignment.CenterStart),
    Right(Alignment.CenterEnd);
}