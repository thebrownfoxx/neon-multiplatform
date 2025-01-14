package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.loader.AnimatedLoadableContent
import com.thebrownfoxx.neon.client.application.ui.component.loader.TextLoader
import com.thebrownfoxx.neon.client.application.ui.extension.copy
import com.thebrownfoxx.neon.client.application.ui.extension.padding
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewHeader
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewHeaderValue
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewListItem
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewStateId
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsState
import com.thebrownfoxx.neon.common.type.Loaded
import com.thebrownfoxx.neon.common.type.Loading
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.Uuid
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.conversations
import neon.client.application.generated.resources.nudged_conversations
import neon.client.application.generated.resources.read_conversations
import neon.client.application.generated.resources.unread_conversations
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatPreviews(
    state: ChatPreviewsState,
    eventHandler: ChatPreviewsEventHandler,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val listState = rememberLazyListState()
    val visibleItems = listState.layoutInfo.visibleItemsInfo
    LaunchedEffect(visibleItems) {
        setLastVisiblePreview(visibleItems, eventHandler.onLastVisiblePreviewChange)
    }

    val ready = state.ready
    LaunchedEffect(ready) {
        if (ready) listState.animateScrollToItem(0)
    }

    // TODO: Highlight the selected conversation
    Surface(modifier = modifier) {
        LazyColumn(
            contentPadding = contentPadding,
            state = listState,
        ) {
            listItems(state.listItems, eventHandler.onConversationClick)
        }
    }
}

private fun setLastVisiblePreview(
    visibleItems: List<LazyListItemInfo>,
    onLastVisiblePreviewChange: (ChatPreviewStateId) -> Unit,
) {
    (visibleItems.lastOrNull()?.key as? String)?.let { previewId ->
        onLastVisiblePreviewChange(ChatPreviewStateId(Uuid(previewId)))
    }
}

private fun LazyListScope.listItems(
    listItems: List<ChatPreviewListItem>,
    onConversationClick: (GroupId) -> Unit,
) {
    if (listItems.firstOrNull() !is ChatPreviewHeader) {
        item {
            Spacer(modifier = Modifier.height(16.dp).animateItem())
        }
    }
    items(
        items = listItems,
        key = { it.key },
    ) {
        ListItem(
            listItem = it,
            onConversationClick = onConversationClick,
        )
    }
}

@Composable
private fun LazyItemScope.ListItem(
    listItem: ChatPreviewListItem,
    onConversationClick: (GroupId) -> Unit,
) {
    Box(modifier = Modifier.animateItem()) {
        when (listItem) {
            is ChatPreviewHeader -> Header(listItem)
            is ChatPreviewState -> ChatPreview(
                state = listItem,
                onClick = {
                    if (listItem.values !is Loaded) return@ChatPreview
                    onConversationClick(listItem.values.value.groupId)
                },
            )
        }
    }
}

@Composable
private fun Header(header: ChatPreviewHeader) {
    val resource = when (header.value) {
        ChatPreviewHeaderValue.NudgedConversations -> Res.string.nudged_conversations
        ChatPreviewHeaderValue.UnreadConversations -> Res.string.unread_conversations
        ChatPreviewHeaderValue.ReadConversations -> Res.string.read_conversations
        ChatPreviewHeaderValue.Conversations -> Res.string.conversations
    }
    Header(
        text = stringResource(resource),
        showPlaceholder = header.showPlaceholder,
    )
}

@Composable
private fun Header(
    text: String,
    showPlaceholder: Boolean,
) {
    val loadable = if (showPlaceholder) Loading else Loaded(text)
    AnimatedLoadableContent(
        targetState = loadable,
        loader = { HeaderLoader(text) },
        modifier = Modifier.padding(16.dp.padding.copy(bottom = 8.dp)),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun HeaderLoader(text: String) {
    TextLoader(
        text = text,
        style = MaterialTheme.typography.titleLarge,
    )
}