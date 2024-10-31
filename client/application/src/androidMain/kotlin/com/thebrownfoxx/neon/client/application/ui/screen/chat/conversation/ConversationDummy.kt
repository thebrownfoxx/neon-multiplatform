package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ChunkTimestamp
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessageEntry
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessageState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessagesScreenState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ReceivedCommunityState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ReceivedDirectState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.SentState
import com.thebrownfoxx.neon.common.extension.ago
import com.thebrownfoxx.neon.common.extension.toLocalDateTime
import com.thebrownfoxx.neon.common.type.Loaded
import com.thebrownfoxx.neon.common.type.Url
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

object ConversationDummy {
    val DirectMessageEntries = listOf(
        timestamp(10.minutes.ago),
        receivedDirectMessage(
            content = "Hey, man. Are you okay?",
            timestamp = 10.minutes.ago,
            lastInGroup = true,
            mustSpace = true,
        ),
        sentDirectMessage(
            content = "are you kidding me?",
            timestamp = 9.minutes.ago,
            firstInGroup = true,
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
            lastInGroup = true,
            mustSpace = true,
        ),
        sentDirectMessage(
            content = "i was not informed the rollercoasters here require kissing",
            timestamp = 7.minutes.ago,
            firstInGroup = true,
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
            lastInGroup = true,
            mustSpace = true,
        ),
        timestamp(5.minutes.ago),
        sentDirectMessage(
            content = "just",
            timestamp = 5.minutes.ago,
            firstInGroup = true,
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
            lastInGroup = true,
        ),
    ).reversed()

    val DirectMessages = ConversationState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "SharlLeclerc",
        ),
        name = "SharlLeclerc",
        entries = Loaded(DirectMessageEntries),
    )

    val MemberScreenState = MessagesScreenState(
        messages = DirectMessages,
        message = "",
    )

    private val oscar = SingleAvatarState(
        url = Url("https://instagram.fmnl4-3.fna.fbcdn.net/v/t51.2885-19/357879596_666316468195257_2748958035059823106_n.jpg?stp=dst-jpg_s150x150&_nc_ht=instagram.fmnl4-3.fna.fbcdn.net&_nc_cat=109&_nc_ohc=Xj_if46SCbUQ7kNvgExdigi&edm=AAGeoI8BAAAA&ccb=7-5&oh=00_AYARW6qNsqlZKR16tuaNlIvRxe2KceOOZiXUT5dp4-klIQ&oe=66CAD6D7&_nc_sid=b0e1a0"),
        placeholder = "oscoala",
    )

    private val stella = SingleAvatarState(
        url = Url("https://sportsbase.io/images/gpfans/copy_1200x800/1bfb2b6af70c6bca2456de50f4f160079cbf886a.png"),
        placeholder = "andreastella",
    )

    val CommunityMessageEntries = listOf(
        timestamp(10.minutes.ago),
        sentCommunityMessage(
            content = "What the fuck just happened?",
            timestamp = 10.minutes.ago,
            firstInGroup = true,
            mustSpace = true,
        ),
        sentCommunityMessage(
            content = "🍿🍿",
            timestamp = 9.minutes.ago,
            senderAvatar = stella,
            firstInGroup = true,
            lastInGroup = true,
            mustSpace = true,
        ),
        sentCommunityMessage(
            content = "I'm sorry, Lando",
            timestamp = 8.minutes.ago,
            senderAvatar = oscar,
            firstInGroup = true,
        ),
        sentCommunityMessage(
            content = "really sorry",
            timestamp = 8.minutes.ago,
            senderAvatar = oscar,
            lastInGroup = true,
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
        firstInGroup: Boolean = false,
        mustSpace: Boolean = false,
    ) = MessageEntry(
        message = MessageState(
            content = content,
            timestamp = timestamp.toLocalDateTime(),
            deliveryState = deliveryState,
            groupPosition = if (firstInGroup) GroupPosition.First else GroupPosition.Middle,
            senderState = SentState,
        ),
        mustSpace = mustSpace,
    )

    private fun receivedDirectMessage(
        content: String,
        timestamp: Instant,
        read: Boolean = true,
        lastInGroup: Boolean = false,
        mustSpace: Boolean = false,
    ) = MessageEntry(
        message = MessageState(
            content = content,
            timestamp = timestamp.toLocalDateTime(),
            deliveryState = if (read) DeliveryState.Read else DeliveryState.Delivered,
            groupPosition = if (lastInGroup) GroupPosition.Last else GroupPosition.Middle,
            senderState = ReceivedDirectState,
        ),
        mustSpace = mustSpace,
    )

    private fun sentCommunityMessage(
        content: String,
        timestamp: Instant,
        deliveryState: DeliveryState = DeliveryState.Read,
        firstInGroup: Boolean = false,
        mustSpace: Boolean = false,
    ) = MessageEntry(
        message = MessageState(
            content = content,
            timestamp = timestamp.toLocalDateTime(),
            deliveryState = deliveryState,
            groupPosition = if (firstInGroup) GroupPosition.First else GroupPosition.Middle,
            senderState = SentState,
        ),
        mustSpace = mustSpace,
    )

    private fun sentCommunityMessage(
        content: String,
        timestamp: Instant,
        senderAvatar: SingleAvatarState,
        read: Boolean = true,
        firstInGroup: Boolean = false,
        lastInGroup: Boolean = false,
        mustSpace: Boolean = false,
    ) = MessageEntry(
        message = MessageState(
            content = content,
            timestamp = timestamp.toLocalDateTime(),
            deliveryState = if (read) DeliveryState.Read else DeliveryState.Delivered,
            groupPosition = when {
                firstInGroup -> GroupPosition.First
                lastInGroup -> GroupPosition.Last
                else -> GroupPosition.Middle
            },
            senderState = ReceivedCommunityState(senderAvatar),
        ),
        mustSpace = mustSpace,
    )
}