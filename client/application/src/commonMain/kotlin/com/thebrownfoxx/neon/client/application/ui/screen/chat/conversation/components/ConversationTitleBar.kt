package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Call
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.avatar.LargeAvatar
import com.thebrownfoxx.neon.client.application.ui.component.loader.AnimatedLoadableContent
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationInfoState
import com.thebrownfoxx.neon.common.type.Loadable
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.call
import neon.client.application.generated.resources.close
import neon.client.application.generated.resources.deleted_group
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConversationTitleBar(
    info: Loadable<ConversationInfoState>,
    onCall: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    AnimatedLoadableContent(
        targetState = info,
        loader = {
            ConversationTitleBarLoader(
                onClose = onClose,
                contentPadding = contentPadding,
            )
        },
        modifier = modifier,
    ) {
        LoadedConversationTitleBar(
            info = it,
            onCall = onCall,
            onClose = onClose,
            contentPadding = contentPadding,
        )
    }
}

@Composable
private fun LoadedConversationTitleBar(
    info: ConversationInfoState,
    onCall: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    with(info) {
        ConversationTitleBarLayout(
            modifier = modifier,
            closeButton = { CloseButton(onClose) },
            callButton = {
                CallButton(onCall)
            },
            contentPadding = contentPadding,
        ) {
            LargeAvatar(avatar = avatar)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = name ?: stringResource(Res.string.deleted_group),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun CloseButton(onClose: () -> Unit) {
    IconButton(onClick = onClose) {
        Icon(
            imageVector = Icons.TwoTone.Close,
            contentDescription = stringResource(Res.string.close),
        )
    }
}

@Composable
private fun CallButton(onCall: () -> Unit) {
    IconButton(onClick = onCall) {
        Icon(
            imageVector = Icons.TwoTone.Call,
            contentDescription = stringResource(Res.string.call),
        )
    }
}
