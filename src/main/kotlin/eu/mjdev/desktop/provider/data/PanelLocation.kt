package eu.mjdev.desktop.provider.data

import androidx.compose.ui.Alignment

@Suppress("unused")
enum class PanelLocation(
    val alignment: Alignment
) {
    Bottom(Alignment.BottomCenter),
    Top(Alignment.TopCenter),
    Left(Alignment.CenterStart),
    Right(Alignment.CenterEnd);
}