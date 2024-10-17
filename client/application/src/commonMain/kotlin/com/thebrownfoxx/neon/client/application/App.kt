package com.thebrownfoxx.neon.client.application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.client.application.ui.component.avatar.LargeAvatar
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    NeonTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            LargeAvatar(
                avatar = SingleAvatarState(
                    url = Url("https://instagram.fmnl9-4.fna.fbcdn.net/v/t51.2885-19/458498312_3341175592685189_4175852103508210717_n.jpg?stp=dst-jpg_s640x640&_nc_ht=instagram.fmnl9-4.fna.fbcdn.net&_nc_cat=105&_nc_ohc=ITkCW9Bil3EQ7kNvgEVuNKF&_nc_gid=8a1acc51ab1d426689bbaa96730773a6&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYB-Ve4Jc2kAnWBzbzkFbNGX40oM-2BsUa5bCffkvgfSUQ&oe=6716BF07&_nc_sid=10d13b"),
                    placeholder = "Lando",
                )
            )
        }
    }
}