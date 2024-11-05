package com.thebrownfoxx.neon.client.application.ui.screen.chat

import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.extension.LocalWindowSizeClass
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.variant.ExpandedChatScreen
import com.thebrownfoxx.neon.client.application.ui.screen.chat.variant.NonExpandedChatScreen

@Composable
fun ChatScreen(
    state: ChatScreenState,
    eventHandler: ChatScreenEventHandler,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = LocalWindowSizeClass.current

    Surface(modifier = modifier) {
        when (windowSizeClass?.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> ExpandedChatScreen(
                state = state,
                eventHandler = eventHandler,
            )

            else -> NonExpandedChatScreen(
                state = state,
                eventHandler = eventHandler,
            )
        }
    }
}