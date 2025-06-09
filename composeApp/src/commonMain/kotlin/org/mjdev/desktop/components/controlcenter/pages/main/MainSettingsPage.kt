package org.mjdev.desktop.components.controlcenter.pages.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.components.custom.DateTime
import org.mjdev.desktop.components.custom.PowerBlock
import org.mjdev.desktop.components.user.UserAvatar
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Colors.darker
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.helpers.compose.Orientation
import org.mjdev.desktop.helpers.shape.BarShape
import org.mjdev.desktop.icons.settings.SettingsHome
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.context.IDesktopContext

@Suppress("FunctionName")
fun MainSettingsPage(
    context: IDesktopContext
) = ControlCenterPage(
    context = context,
    icon = SettingsHome,
    name = "Home",
    showHeader = false
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier.clip(RectangleShape),
                contentAlignment = Alignment.BottomStart
            ) {
                val shape = BarShape(
                    offset = 80.dp,
                    circleRadius = 52.dp,
                    cornerRadius = 0.dp,
                    circleGap = 4.dp,
                )
                val brush = Brush.horizontalGradient(
                    listOf(
                        backgroundColor.darker(0.1f),
                        backgroundColor.darker(0.1f),
                        backgroundColor.darker(0.1f),
                        backgroundColor.alpha(0.9f),
                        backgroundColor.alpha(0.7f),
                        backgroundColor.alpha(0.3f),
                        backgroundColor.darker(0.1f).alpha(0.5f),
                    ),
//                    startX = 0f,
//                    endX = 0f
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            brush = brush,
                            shape = shape
                        )
                )
                UserAvatar(
                    avatarSize = 96.dp,
                    titleTextSize = 20.sp,
                    detailTextSize = 16.sp,
                    onUserAvatarClick = {
                        // todo
                    },
                    orientation = Orientation.Horizontal
                )
            }
            HorizontalDivider(
                color = borderColor.alpha(0.5f),
                thickness = 2.dp
            )
            DateTime(
                modifier = Modifier.fillMaxWidth(),
                timeTextColor = textColor,
                dateTextColor = textColor,
                backgroundColor = backgroundColor
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
//            FlexibleBottomSheet(
//                onDismissRequest = {},
//                sheetState = rememberFlexibleBottomSheetState(
//                    flexibleSheetSize = FlexibleSheetSize(
//                        fullyExpanded = 0.9f,
//                        intermediatelyExpanded = 0.5f,
//                        slightlyExpanded = 0.15f,
//                    ),
//                    isModal = true,
//                    skipSlightlyExpanded = true,
//                ),
//                containerColor = backgroundColor,
//            ) {
//                Text(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp),
//                    text = "This is Flexible Bottom Sheet",
//                    textAlign = TextAlign.Center,
//                    color = Color.White,
//                )
//            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
            ) {
                PowerBlock(
                    shadowColor = borderColor.alpha(0.3f),
                    onPowerButtonClick = {
                        // todo : dialog
                        runAsync {
                            context.logOut()
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun MainSettingsPagePreview() = preview {
    MainSettingsPage(context).Render()
}
