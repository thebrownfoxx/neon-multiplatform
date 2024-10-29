package com.thebrownfoxx.neon.client.application.ui.screen.conversations.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        Surface {
            AvatarWithDelivery(
                avatar = SingleAvatarState(
                    url = null,
                    placeholder = "Lando",
                ),
                delivery = DeliveryState.Read,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}