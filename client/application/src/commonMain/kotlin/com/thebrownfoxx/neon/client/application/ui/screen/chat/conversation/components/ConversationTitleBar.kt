package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Call
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.AppBar
import com.thebrownfoxx.neon.client.application.ui.component.TopBarScrim
import com.thebrownfoxx.neon.client.application.ui.component.avatar.LargeAvatar
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.close
import neon.client.application.generated.resources.deleted_group
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConversationTitleBar(
    avatar: AvatarState?,
    name: String?,
    onCall: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ConversationTitleBarLayout(
        modifier = modifier,
        closeButton = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.TwoTone.Close,
                    contentDescription = stringResource(Res.string.close),
                )
            }
        },
        callButton = {
            IconButton(onClick = onCall) {
                Icon(imageVector = Icons.TwoTone.Call, contentDescription = null)
            }
        },
    ) {
        LargeAvatar(avatar = avatar)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = name ?: stringResource(Res.string.deleted_group),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun ConversationTitleBarLayout(
    closeButton: @Composable () -> Unit,
    callButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    TopBarScrim(modifier = modifier) {
        AppBar(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
            ) {
                closeButton()
                content()
                Spacer(modifier = Modifier.weight(1f))
                callButton()
            }
        }
    }
}