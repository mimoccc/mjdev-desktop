package eu.mjdev.desktop.helpers.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.materialkolor.ktx.themeColor
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.extensions.Image.loadPicture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Suppress("unused")
class Palette(
    private val scope: CoroutineScope,
    private val baseColor: Color = Color.Transparent
) {
    var backgroundColor: Color = Color.SuperDarkGray

    fun update(src: Any?) = scope.launch {
        loadPicture(src)
            .getOrNull()
            ?.themeColor(fallback = baseColor)?.also { color ->
                backgroundColor = color
            }
    }

    companion object {
        @Composable
        fun rememberPalette(
            backgroundColor: Color = Color.SuperDarkGray,
            scope: CoroutineScope = rememberCoroutineScope()
        ) = remember(scope, backgroundColor) { Palette(scope, backgroundColor) }
    }
}

