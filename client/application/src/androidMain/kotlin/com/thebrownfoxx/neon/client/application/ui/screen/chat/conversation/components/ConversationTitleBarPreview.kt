package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import com.thebrownfoxx.neon.common.type.Url

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        ConversationTitleBar(
            avatar = SingleAvatarState(
                url = Url("https://instagram.fmnl8-6.fna.fbcdn.net/v/t51.2885-19/459092883_500108879575907_8769539872090278916_n.jpg?stp=dst-jpg_s150x150&_nc_ht=instagram.fmnl8-6.fna.fbcdn.net&_nc_cat=1&_nc_ohc=t9VaGZqBG1cQ7kNvgEyofTs&_nc_gid=fe103e14586c40b4947ffa540c9d288c&edm=AAGeoI8BAAAA&ccb=7-5&oh=00_AYBtQZgIGCXaw1O4cVuC38H2n1IUwfI33oM-aeCKnuGRKg&oe=66EB38E4&_nc_sid=b0e1a0"),
                placeholder = "SharlLeclerc",
            ),
            name = "SharlLeclerc",
            onClose = {},
            onCall = {},
        )
    }
}