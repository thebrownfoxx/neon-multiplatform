package com.thebrownfoxx.neon.client.application.ui.component.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.GroupAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.loader.LargeAvatarLoader
import com.thebrownfoxx.neon.client.application.ui.component.loader.MediumAvatarLoader
import com.thebrownfoxx.neon.client.application.ui.component.loader.SmallAvatarLoader
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class AvatarSize(val dp: Dp) {
    Large(dp = 40.dp),
    Medium(dp = 24.dp),
    Small(dp = 16.dp),
}

@Composable
private fun Avatar(
    url: Url?,
    contentDescription: String?,
    loading: @Composable () -> Unit,
    error: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = CircleShape,
    ) {
        SubcomposeAsyncImage(
            loading = { loading() },
            error = { error() },
            model = url?.value,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun LargeAvatar(
    url: Url?,
    @Suppress("SameParameterValue") contentDescription: String?,
    loading: @Composable () -> Unit,
    error: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Avatar(
        url = url,
        contentDescription = contentDescription,
        loading = loading,
        error = error,
        modifier = modifier.size(AvatarSize.Large.dp),
    )
}

@Composable
private fun MediumAvatar(
    url: Url?,
    @Suppress("SameParameterValue") contentDescription: String?,
    loading: @Composable () -> Unit,
    error: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Avatar(
        url = url,
        contentDescription = contentDescription,
        loading = loading,
        error = error,
        modifier = modifier.size(AvatarSize.Medium.dp),
    )
}

@Composable
private fun SmallAvatar(
    url: Url?,
    contentDescription: String?,
    loading: @Composable () -> Unit,
    error: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Avatar(
        url = url,
        contentDescription = contentDescription,
        loading = loading,
        error = error,
        modifier = modifier.size(AvatarSize.Small.dp),
    )
}

@Composable
private fun GroupAvatarBox(
    frontAvatar: @Composable () -> Unit,
    backAvatar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    clippingBackground: Color = MaterialTheme.colorScheme.surface,
) {
    Box(modifier = modifier.size(AvatarSize.Large.dp)) {
        Box(modifier = Modifier.align(Alignment.BottomStart)) {
            backAvatar()
        }
        Box(
            modifier = Modifier
                .offset(x = 2.dp, y = (-2).dp)
                .size(AvatarSize.Medium.dp + 4.dp)
                .clip(CircleShape)
                .background(clippingBackground)
                .align(Alignment.TopEnd)
                .offset(x = 2.dp, y = 2.dp),
        ) {
            frontAvatar()
        }
    }
}

@Composable
private fun SingleLargeAvatar(
    avatar: SingleAvatarState?,
    modifier: Modifier = Modifier,
) {
    LargeAvatar(
        url = avatar?.url,
        contentDescription = null,
        loading = { LargeAvatarLoader() },
        error = { LargePlaceholderAvatar(placeholder = avatar?.placeholder) },
        modifier = modifier,
    )
}

@Composable
private fun GroupLargeAvatar(
    avatar: GroupAvatarState?,
    modifier: Modifier = Modifier,
) {
    GroupAvatarBox(
        frontAvatar = { MediumAvatar(avatar = avatar?.front) },
        backAvatar = { MediumAvatar(avatar = avatar?.back) },
        modifier = modifier,
    )
}

@Composable
fun LargeAvatar(
    avatar: AvatarState?,
    modifier: Modifier = Modifier,
) {
    when (avatar) {
        null -> SingleLargeAvatar(avatar = null, modifier = modifier)
        is SingleAvatarState -> SingleLargeAvatar(avatar = avatar, modifier = modifier)
        is GroupAvatarState -> GroupLargeAvatar(avatar = avatar, modifier = modifier)
    }
}

@Composable
fun MediumAvatar(
    avatar: SingleAvatarState?,
    modifier: Modifier = Modifier,
) {
    MediumAvatar(
        url = avatar?.url,
        contentDescription = null,
        loading = { MediumAvatarLoader() },
        error = { MediumPlaceholderAvatar(placeholder = avatar?.placeholder) },
        modifier = modifier,
    )
}

@Composable
fun SmallAvatar(
    avatar: SingleAvatarState?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    SmallAvatar(
        url = avatar?.url,
        contentDescription = contentDescription,
        loading = { SmallAvatarLoader() },
        error = { SmallPlaceholderAvatar(placeholder = avatar?.placeholder) },
        modifier = modifier,
    )
}

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