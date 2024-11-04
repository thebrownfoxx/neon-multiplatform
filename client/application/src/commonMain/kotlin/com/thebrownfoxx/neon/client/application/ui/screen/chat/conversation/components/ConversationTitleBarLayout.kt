package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.AppBar
import com.thebrownfoxx.neon.client.application.ui.component.TopBarScrim

@Composable
fun ConversationTitleBarLayout(
    closeButton: @Composable () -> Unit,
    callButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable RowScope.() -> Unit,
) {
    TopBarScrim(modifier = modifier) {
        AppBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.padding(contentPadding),
        ) {
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