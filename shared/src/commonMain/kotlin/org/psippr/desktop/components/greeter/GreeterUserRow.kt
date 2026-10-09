package org.psippr.desktop.components.greeter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.psippr.desktop.components.user.UserAvatar
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.helpers.compose.Orientation
import org.psippr.desktop.interfaces.IUser

/** Avatar size of the selected user. */
private val SELECTED_AVATAR_SIZE = 128.dp

/** Avatar size of the other users. */
private val OTHER_AVATAR_SIZE = 96.dp

/** Ring width around an avatar. */
private val AVATAR_BORDER = 4.dp

/** Gap between two users in the row. */
private val USER_GAP = 24.dp

/** Width of the slot of one user: equal slots let the selected one sit exactly in the center. */
private val USER_SLOT_WIDTH = 160.dp

/** Divisor giving half of a width. */
private const val HALF = 2

/** Opacity of the users that are not selected. */
private const val OTHER_ALPHA = 0.5f

/** Opacity of the selected user. */
private const val SELECTED_ALPHA = 1f

// old: the users were centered as a group, so the selected one sat off center
///**
// * All [users] of the login screen as avatars in one horizontal, scrollable row, centered while
// * they fit. [selected] is drawn larger and opaque and is kept scrolled into view; a click on an
// * avatar reports its index to [onSelect].
// */
//@Composable
//fun GreeterUserRow(
//    users: List<IUser>,
//    selected: Int,
//    onSelect: (index: Int) -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    val listState = rememberLazyListState()
//    LaunchedEffect(selected) {
//        if (selected in users.indices) listState.animateScrollToItem(selected)
//    }
//    LazyRow(
//        modifier = modifier.fillMaxWidth(),
//        state = listState,
//        horizontalArrangement = Arrangement.spacedBy(USER_GAP, Alignment.CenterHorizontally),
//        verticalAlignment = Alignment.Bottom,
//    ) {
//        itemsIndexed(users) { index, user ->
//            val isSelected = index == selected
//            UserAvatar(
//                modifier = Modifier.alpha(if (isSelected) SELECTED_ALPHA else OTHER_ALPHA),
//                user = user,
//                avatarSize = if (isSelected) SELECTED_AVATAR_SIZE else OTHER_AVATAR_SIZE,
//                orientation = Orientation.Vertical,
//                textAlign = TextAlign.Center,
//                circleBorder = AVATAR_BORDER,
//                onUserAvatarClick = { onSelect(index) },
//            )
//        }
//    }
//}

/**
 * All [users] of the login screen as avatars in one horizontal, scrollable row. [selected] is
 * drawn larger and opaque and always scrolled to the center of the screen, above the password
 * field; the others sit beside it. A click on an avatar reports its index to [onSelect].
 */
@Composable
fun GreeterUserRow(
    users: List<IUser>,
    selected: Int,
    onSelect: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) = BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val listState = rememberLazyListState()
    // half the row minus half a slot on both sides: scrolling an item to the start centers it
    val sidePadding = ((maxWidth - USER_SLOT_WIDTH) / HALF).coerceAtLeast(0.dp)
    LaunchedEffect(selected) {
        if (selected in users.indices) listState.animateScrollToItem(selected)
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        contentPadding = PaddingValues(horizontal = sidePadding),
        horizontalArrangement = Arrangement.spacedBy(USER_GAP),
        verticalAlignment = Alignment.Bottom,
    ) {
        itemsIndexed(users) { index, user ->
            val isSelected = index == selected
            Box(
                modifier = Modifier.width(USER_SLOT_WIDTH),
                contentAlignment = Alignment.BottomCenter,
            ) {
                UserAvatar(
                    modifier = Modifier.alpha(if (isSelected) SELECTED_ALPHA else OTHER_ALPHA),
                    user = user,
                    avatarSize = if (isSelected) SELECTED_AVATAR_SIZE else OTHER_AVATAR_SIZE,
                    orientation = Orientation.Vertical,
                    textAlign = TextAlign.Center,
                    circleBorder = AVATAR_BORDER,
                    onUserAvatarClick = { onSelect(index) },
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewGreeterUserRow() = preview(640, 240) {
    GreeterUserRow(users = listOf(IUser.DEFAULT, IUser.DEFAULT), selected = 0, onSelect = {})
}
