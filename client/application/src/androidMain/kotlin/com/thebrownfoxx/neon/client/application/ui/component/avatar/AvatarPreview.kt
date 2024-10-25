package com.thebrownfoxx.neon.client.application.ui.component.avatar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.GroupAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme


private val singleAvatarState = SingleAvatarState(
    url = null,
    placeholder = "W",
)

@Preview
@Composable
private fun LargePreview() {
    NeonTheme {
        LargeAvatar(
            avatar = singleAvatarState,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun MediumPreview() {
    NeonTheme {
        MediumAvatar(
            avatar = singleAvatarState,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun SmallPreview() {
    NeonTheme {
        SmallAvatar(
            avatar = singleAvatarState,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun GroupPreview() {
    NeonTheme {
        Surface {
            LargeAvatar(
                avatar = GroupAvatarState(
                    front = singleAvatarState,
                    back = singleAvatarState,
                ),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}