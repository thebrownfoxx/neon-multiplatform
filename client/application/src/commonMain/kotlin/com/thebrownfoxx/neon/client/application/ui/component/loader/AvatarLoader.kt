package com.thebrownfoxx.neon.client.application.ui.component.loader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.avatar.AvatarSize
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun AvatarLoader(
    size: AvatarSize,
    modifier: Modifier = Modifier,
) {
    Loader { color ->
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
fun LargeAvatarLoader(modifier: Modifier = Modifier) {
    AvatarLoader(
        size = AvatarSize.Large,
        modifier = modifier,
    )
}

@Composable
fun MediumAvatarLoader(modifier: Modifier = Modifier) {
    AvatarLoader(
        size = AvatarSize.Medium,
        modifier = modifier,
    )
}

@Composable
fun SmallAvatarLoader(modifier: Modifier = Modifier) {
    AvatarLoader(
        size = AvatarSize.Small,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        LargeAvatarLoader(modifier = Modifier.padding(16.dp))
    }
}