package eu.mjdev.desktop.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.materialkolor.ktx.themeColors
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.extensions.Image.loadPicture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("unused")
class Palette(
    private val baseColor: Color = Color.Transparent
) {
    private var colorsFromImage: List<Color> = listOf(baseColor)

    val backgroundColor: Color
        get() = runCatching {
            colorsFromImage.first()
        }.getOrDefault(baseColor)

    fun update(src: Any?) {
        CoroutineScope(Dispatchers.IO).launch {
            loadPicture(src)
                .getOrNull()
                ?.themeColors(fallback = baseColor)?.also { colors ->
                    colorsFromImage = colors.sortedBy {
                        it.value
                    }
                }
        }
    }

    companion object {
        @Composable
        fun rememberPalette(
            backgroundColor: Color = Color.SuperDarkGray
        ) = remember(backgroundColor) { Palette(backgroundColor) }
    }
}

