package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.Spacer
import com.thebrownfoxx.neon.client.application.ui.component.loader.LargeAvatarLoader
import com.thebrownfoxx.neon.client.application.ui.component.loader.TextLoader
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.close
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConversationTitleBarLoader(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    ConversationTitleBarLayout(
        modifier = modifier,
        closeButton = { CloseButton(onClose) },
        callButton = {},
        contentPadding = contentPadding,
    ) {
        LargeAvatarLoader()
        Spacer(width = 16.dp)
        TextLoader(
            text = "The Brown Foxx Org",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(width = 16.dp)
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