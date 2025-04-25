package org.mjdev.desktop.components.desktoppanel.applets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.fonticon.FontIcon
import org.mjdev.desktop.extensions.ButtonDefaults.color
import org.mjdev.desktop.extensions.ButtonDefaults.noElevation
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.Modifier.circleBorder
import org.mjdev.desktop.extensions.Modifier.clipRect
import org.mjdev.desktop.extensions.Modifier.onLeftClick
import org.mjdev.desktop.extensions.Modifier.onMouseEnter
import org.mjdev.desktop.extensions.Modifier.onMouseLeave
import org.mjdev.desktop.extensions.Modifier.onMousePress
import org.mjdev.desktop.extensions.Modifier.onRightClick
import org.mjdev.desktop.extensions.MutableStateExt.rememberCalculated
import org.mjdev.desktop.extensions.MutableStateExt.rememberState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.interfaces.IApp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DesktopPanelIcon(
    app: IApp? = null,
    icon: String? = null,
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    iconBackgroundHover: Color = Color.White,
    iconColorRunning: Color = Color.White,
    iconSize: DpSize = DpSize(48.dp, 48.dp),
    iconPadding: PaddingValues = PaddingValues(2.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    iconState: MutableState<Boolean> = rememberState(false),
    contentDescription: String? = null,
    onTooltip: (item: Any?) -> Unit = {},
    onContextMenuClick: () -> Unit = {},
    onClick: () -> Unit = {},
) = withDesktopContext {
    val materialIcon: Int = remember(app, icon) {
        val iconName = app?.name ?: icon // todo better guess
        iconSet.iconForName(iconName) ?: 0
    }
    val background = when {
        iconState.value -> iconBackgroundHover
        else -> Color.Transparent
    }
    val isStarting by rememberCalculated(app) { app?.isStarting ?: false }
    val isRunning by rememberCalculated(app) {
        app?.isRunning == true || processManager.hasAppProcess(app)
    }
    Box(
        modifier = Modifier
            .size(iconSize)
            .clipRect()
            .onMouseEnter {
                iconState.value = true
                onTooltip(app ?: contentDescription)
            }
            .onMouseLeave {
                iconState.value = false
            }
            .onMousePress {
                iconState.value = false
                onLeftClick {
                    onClick()
                }
                onRightClick {
                    onContextMenuClick()
                }
            }
    ) {
        Button(
            modifier = Modifier.fillMaxSize()
                .circleBorder(
                    2.dp,
                    textColor.alpha(0.5f)
                ),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.color(background),
            elevation = ButtonDefaults.noElevation(),
            onClick = {}
        ) {
            FontIcon(
                modifier = Modifier.fillMaxSize(),
                iconId = materialIcon,
                iconColor = iconColor,
                iconBackgroundColor = if (isRunning) iconColorRunning else iconBackgroundColor,
                iconSize = iconSize,
                innerPadding = iconPadding,
                outerPadding = iconOuterPadding,
            )
        }
        if (isStarting || isRunning) {
            CircularProgressIndicator(
                modifier = Modifier.padding(4.dp).fillMaxSize(),
                color = iconColor,
                strokeWidth = 4.dp,
                backgroundColor = Color.Transparent
            )
        }
    }
}

@Preview
@Composable
fun DesktopPanelIconPreview() = preview {
    DesktopPanelIcon(
        app = null //App.Test
    )
}
