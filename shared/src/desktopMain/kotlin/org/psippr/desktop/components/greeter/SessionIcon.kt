package org.psippr.desktop.components.greeter

import androidx.compose.foundation.layout.size
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.psippr.desktop.components.image.ImageAny
import org.psippr.desktop.helpers.greetd.LoginSession

/** Default edge length of a session picture. */
private val DEFAULT_ICON_SIZE = 24.dp

// old: vector icons always tinted with the screen text color - white, invisible in the white menu
// fun SessionIcon(session: LoginSession, size: Dp = DEFAULT_ICON_SIZE) = withDesktopContext {
//    val icon = session.icon
//    ImageAny(
//        src = icon,
//        modifier = Modifier.size(size),
//        contentDescription = session.name,
//        colorFilter = if (icon is ImageVector) ColorFilter.tint(textColor) else null,
//    )
// }

/**
 * Picture of a login [session]: its logo file, or its vector icon tinted with [tint] (by default
 * the content color of the surrounding surface, so it stays visible in the menu and on screen).
 */
@Composable
fun SessionIcon(
    session: LoginSession,
    size: Dp = DEFAULT_ICON_SIZE,
    tint: Color = LocalContentColor.current,
) {
    val icon = session.icon
    ImageAny(
        src = icon,
        modifier = Modifier.size(size),
        contentDescription = session.name,
        colorFilter = if (icon is ImageVector) ColorFilter.tint(tint) else null,
    )
}
