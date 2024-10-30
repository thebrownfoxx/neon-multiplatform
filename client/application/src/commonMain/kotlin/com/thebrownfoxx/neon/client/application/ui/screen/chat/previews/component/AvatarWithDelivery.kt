package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.avatar.LargeAvatar
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.DeliveryIndicator
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState

@Composable
fun AvatarWithDelivery(
    avatar: AvatarState?,
    modifier: Modifier = Modifier,
    delivery: DeliveryState? = null
) {
    val clippingBackground = MaterialTheme.colorScheme.surface

    AvatarWithDeliveryBox(
        statusIndicator = {
            if (delivery != null) {
                DeliveryIndicator(
                    delivery = delivery,
                    clippingBackground = clippingBackground,
                )
            }
        },
        modifier = modifier,
    ) {
        LargeAvatar(avatar = avatar)
    }
}

@Composable
fun AvatarWithDeliveryBox(
    statusIndicator: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp),
        ) {
            statusIndicator()
        }
    }
}