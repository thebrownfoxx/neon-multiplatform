package com.thebrownfoxx.neon.client.application.ui.screen.chat.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ChunkTimestamp
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationInfoState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessageEntry
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessageState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ReceivedCommunityMessageState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ReceivedDirectMessageState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.SentMessageState
import com.thebrownfoxx.neon.common.extension.ago
import com.thebrownfoxx.neon.common.extension.toLocalDateTime
import com.thebrownfoxx.neon.common.type.Loaded
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

object ConversationDummy {
    val DirectMessageEntries = listOf(
        timestamp(10.minutes.ago),
        receivedDirectMessage(
            content = "Hey, man. Are you okay?",
            timestamp = 10.minutes.ago,
            groupPosition = GroupPosition.First,
            mustSpace = true,
        ),
        sentDirectMessage(
            content = "are you kidding me?",
            timestamp = 9.minutes.ago,
            groupPosition = GroupPosition.Last,
        ),
        sentDirectMessage(
            content = "what do you think?",
            timestamp = 9.minutes.ago,
            mustSpace = true,
        ),
        receivedDirectMessage(
            content = "I just wanted to try the roller coaster",
            timestamp = 8.minutes.ago,
        ),
        receivedDirectMessage(
            content = "Come, on, mate.",
            timestamp = 8.minutes.ago,
            groupPosition = GroupPosition.First,
            mustSpace = true,
        ),
        sentDirectMessage(
            content = "i was not informed the rollercoasters here require kissing",
            timestamp = 7.minutes.ago,
            groupPosition = GroupPosition.Last,
            mustSpace = true,
        ),
        receivedDirectMessage(
            content = "What?",
            timestamp = 7.minutes.ago,
        ),
        receivedDirectMessage(
            content = "What kissing?",
            timestamp = 7.minutes.ago,
        ),
        receivedDirectMessage(
            content = "Why the hell would I kiss Carlos?",
            timestamp = 6.minutes.ago,
        ),
        receivedDirectMessage(
            content = "You know that I'm with Pierre",
            timestamp = 6.minutes.ago,
        ),
        receivedDirectMessage(
            content = "Come on, now, mate.",
            timestamp = 6.minutes.ago,
            groupPosition = GroupPosition.First,
            mustSpace = true,
        ),
        timestamp(5.minutes.ago),
        sentDirectMessage(
            content = "just",
            timestamp = 5.minutes.ago,
            groupPosition = GroupPosition.Last,
        ),
        sentDirectMessage(
            content = "GET TF AWAY FROM CARLOS",
            timestamp = 5.minutes.ago,
            mustSpace = true,
        ),
        receivedDirectMessage(
            content = "Won't happen again.",
            timestamp = 3.minutes.ago,
            read = false,
        ),
        receivedDirectMessage(
            content = "I'm sorry.",
            timestamp = 3.minutes.ago,
            read = false,
            groupPosition = GroupPosition.First,
        ),
    ).reversed()

    val DirectMessages = ConversationState(
        info = Loaded(
            ConversationInfoState(
                avatar = SingleAvatarState(
                    url = null,
                    placeholder = "SharlLeclerc",
                ),
                name = "SharlLeclerc",
            )
        ),
        entries = DirectMessageEntries,
        loading = false,
    )

    val ConversationPaneState = ConversationPaneState(
        conversation = DirectMessages,
        message = "",
    )

    private val oscar = SingleAvatarState(
        url = null,
        placeholder = "oscoala",
    )

    private val stella = SingleAvatarState(
        url = null,
        placeholder = "andreastella",
    )

    val CommunityMessageEntries = listOf(
        timestamp(10.minutes.ago),
        sentCommunityMessage(
            content = "What the fuck just happened?",
            timestamp = 10.minutes.ago,
            groupPosition = GroupPosition.Last,
            mustSpace = true,
        ),
        sentCommunityMessage(
            content = "🍿🍿",
            timestamp = 9.minutes.ago,
            senderAvatar = stella,
            groupPosition = GroupPosition.Alone,
            mustSpace = true,
        ),
        sentCommunityMessage(
            content = "I'm sorry, Lando",
            timestamp = 8.minutes.ago,
            senderAvatar = oscar,
            groupPosition = GroupPosition.Last,
        ),
        sentCommunityMessage(
            content = "really sorry",
            timestamp = 8.minutes.ago,
            senderAvatar = oscar,
            groupPosition = GroupPosition.First,
        ),
    ).reversed()

//    val CommunityScreenState = MessagesScreenState(
//        messages = CommunityMessagesState(
//            conversation = CommunityConversationState(
//                community = CommunityState(
//                    avatarUrl = Url("https://static.wikia.nocookie.net/f1-formula-1/images/e/ed/McLaren.jpg/revision/latest?cb=20230118201145"),
//                    name = "McLaren",
//                )
//            ),
//            entries = CommunityMessageEntries,
//        ),
//        message = "fuck you oscar",
//    )

    private fun timestamp(timestamp: Instant) =
        ChunkTimestamp(timestamp = timestamp.toLocalDateTime())

    private fun sentDirectMessage(
        content: String,
        timestamp: Instant,
        deliveryState: DeliveryState = DeliveryState.Read,
        groupPosition: GroupPosition = GroupPosition.Middle,
        mustSpace: Boolean = false,
    ) = MessageEntry(
        message = MessageState(
            content = content,
            timestamp = timestamp.toLocalDateTime(),
            delivery = deliveryState,
            groupPosition = groupPosition,
            sender = SentMessageState,
        ),
        mustSpace = mustSpace,
    )

    private fun receivedDirectMessage(
        content: String,
        timestamp: Instant,
        read: Boolean = true,
        groupPosition: GroupPosition = GroupPosition.Middle,
        mustSpace: Boolean = false,
    ) = MessageEntry(
        message = MessageState(
            content = content,
            timestamp = timestamp.toLocalDateTime(),
            delivery = if (read) DeliveryState.Read else DeliveryState.Delivered,
            groupPosition = groupPosition,
            sender = ReceivedDirectMessageState,
        ),
        mustSpace = mustSpace,
    )

    private fun sentCommunityMessage(
        content: String,
        timestamp: Instant,
        deliveryState: DeliveryState = DeliveryState.Read,
        groupPosition: GroupPosition = GroupPosition.Middle,
        mustSpace: Boolean = false,
    ) = MessageEntry(
        message = MessageState(
            content = content,
            timestamp = timestamp.toLocalDateTime(),
            delivery = deliveryState,
            groupPosition = groupPosition,
            sender = SentMessageState,
        ),
        mustSpace = mustSpace,
    )

    private fun sentCommunityMessage(
        content: String,
        timestamp: Instant,
        senderAvatar: SingleAvatarState,
        read: Boolean = true,
        groupPosition: GroupPosition = GroupPosition.Middle,
        mustSpace: Boolean = false,
    ) = MessageEntry(
        message = MessageState(
            content = content,
            timestamp = timestamp.toLocalDateTime(),
            delivery = if (read) DeliveryState.Read else DeliveryState.Delivered,
            groupPosition = groupPosition,
            sender = ReceivedCommunityMessageState(senderAvatar),
        ),
        mustSpace = mustSpace,
    )
}