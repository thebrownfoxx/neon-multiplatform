package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.extension.PaddingSide
import com.thebrownfoxx.neon.client.application.ui.extension.except
import com.thebrownfoxx.neon.client.application.ui.extension.minus
import com.thebrownfoxx.neon.client.application.ui.extension.plus
import com.thebrownfoxx.neon.client.application.ui.extension.topPadding
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component.ChatPreviews
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component.FakeSearchBar
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsState

@Composable
fun ChatPreviewsPane(
    state: ChatPreviewsState,
    eventHandler: ChatPreviewsEventHandler,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Scaffold(
        topBar = {
            FakeSearchBar(contentPadding = contentPadding.except(PaddingSide.Bottom))
        },
        modifier = modifier,
    ) { innerPadding ->
        ChatPreviews(
            state = state,
            eventHandler = eventHandler,
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding - 16.dp.topPadding +
                    contentPadding.except(PaddingSide.Top),
        )
    }
}