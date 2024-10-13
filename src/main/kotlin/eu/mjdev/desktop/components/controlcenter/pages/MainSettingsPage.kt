package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import eu.mjdev.desktop.components.custom.DateTime
import eu.mjdev.desktop.components.custom.PowerBlock
import eu.mjdev.desktop.components.user.UserAvatar
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.ColorUtils.darker
import eu.mjdev.desktop.extensions.Compose.runAsync
import eu.mjdev.desktop.extensions.Modifier.rectShadow
import eu.mjdev.desktop.helpers.shape.BarShape

@Suppress("FunctionName")
fun MainSettingsPage() = ControlCenterPage(
    icon = Icons.Filled.Home,
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
                    offset = 68f,
                    circleRadius = 52.dp,
                    cornerRadius = 0.dp,
                    circleGap = 4.dp,
                )
                val brush = Brush.horizontalGradient(
                    listOf(
                        backgroundColor.alpha(1f),
                        backgroundColor.alpha(1f),
                        backgroundColor.darker(0.3f),
                        backgroundColor.alpha(0.9f),
                        backgroundColor.alpha(0.7f)
                    ),
                    startX = 0f,
                    endX = 0f
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
                    titleHorizontalAlignment = Alignment.CenterHorizontally,
                    onUserAvatarClick = {
                        // todo
                    }
                )
            }
            Divider(
                modifier = Modifier.fillMaxWidth()
                    .height(2.dp),
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
                    backgroundColor = backgroundColor,
                    shadowColor = borderColor.alpha(0.3f),
                    iconTintColor = iconsTintColor,
                    onPowerButtonClick = {
                        // todo : dialog
                        runAsync {
                            api.logOut()
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun MainSettingsPagePreview() = MainSettingsPage().render()
