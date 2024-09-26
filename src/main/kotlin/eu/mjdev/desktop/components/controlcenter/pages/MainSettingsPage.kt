package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import eu.mjdev.desktop.components.custom.DateTime
import eu.mjdev.desktop.components.custom.PowerBlock
import eu.mjdev.desktop.components.custom.UserAvatar
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.Modifier.rectShadow

@Suppress("FunctionName")
fun MainSettingsPage() = ControlCenterPage(
    icon = Icons.Filled.Home,
    name = "Home"
)  {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .rectShadow(8.dp, borderColor.copy(alpha = 0.3f))
        ) {
            UserAvatar(
                modifier = Modifier.background(backgroundColor),
                avatarSize = 128.dp,
                orientation = Orientation.Vertical,
                onUserAvatarClick = {
                    // todo
                }
            )
            Divider(
                modifier = Modifier.fillMaxWidth()
                    .height(2.dp),
                color = borderColor,
                thickness = 2.dp
            )
            DateTime(
                textColor = textColor,
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
//                    textColor = textColor,
                    onPowerButtonClick = {
                        // todo : dialog
                        api.shutdown()
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun MainSettingsPagePreview() = MainSettingsPage().render()
