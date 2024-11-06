package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.GroupAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.common.extension.ago
import com.thebrownfoxx.neon.common.extension.toLocalDateTime
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

object ChatPreviewsDummy {
    private val oconGroupAvatar = GroupAvatarState(
        front = SingleAvatarState(
            url = null,
            placeholder = "oconman",
        ),
        back = SingleAvatarState(
            url = null,
            placeholder = "logie_bear",
        ),
    )

    val NudgedConversations = listOf(
        receivedDirectChatPreview(
            name = "AndreaStella",
            message = "where the feet pics",
            timestamp = 1.days.ago,
            emphasized = true,
        ),
        receivedDirectChatPreview(
            name = "will_joseph",
            message = "feet pics or im sabotaging your next race",
            timestamp = 2.days.ago,
            emphasized = true,
        ),
    ).sortedByDescending { it.content?.timestamp }

    val UnreadConversations = listOf(
        receivedCommunityChatPreview(
            avatar = SingleAvatarState(
                url = null,
                placeholder = "McLaren",
            ),
            name = "McLaren",
            message = "I'm sorry Lando",
            timestamp = 14.minutes.ago,
            sender = SingleAvatarState(
                url = null,
                placeholder = "oscoala",
            ),
            emphasized = true,
        ),
        receivedDirectChatPreview(
            name = "carlito",
            message = "you ready to get freaky? 🤤",
            timestamp = 5.minutes.ago,
            emphasized = true,
        ),
    ).sortedByDescending { it.content?.timestamp }

    val ReadConversations = listOf(
        sentDirectChatPreview(
            name = "SharlLeclair",
            message = "GET AWAY FROM CARLOS",
            timestamp = 7.minutes.ago,
            delivery = DeliveryState.Read,
        ),
        sentCommunityChatPreview(
            avatar = oconGroupAvatar,
            name = "Cool Ones",
            message = "STOP ADDING ME TO COMMUNITIES OCON!",
            timestamp = 18.minutes.ago,
            delivery = DeliveryState.Failed,
        ),
        sentDirectChatPreview(
            name = "MaxFewtrell",
            message = "im naked rn 😉",
            timestamp = 2.minutes.ago,
            delivery = DeliveryState.Reacted("🤤"),
        ),
        sentDirectChatPreview(
            name = "logie_bear",
            message = "yea im blocking ocon",
            timestamp = 3.minutes.ago,
            delivery = DeliveryState.Delivered,
        ),
        receivedDirectChatPreview(
            name = "oscoala",
            message = "I'm sorry Lando.",
            timestamp = 20.minutes.ago,
        ),
        sentCommunityChatPreview(
            avatar = GroupAvatarState(
                front = SingleAvatarState(
                    url = null,
                    placeholder = "carlito",
                ),
                back = SingleAvatarState(
                    url = null,
                    placeholder = "MaxFewtrell",
                ),
            ),
            name = "Freaky Times",
            message = "ready to get freaky?",
            timestamp = 30.minutes.ago,
            delivery = DeliveryState.Sent,
        ),
        sentCommunityChatPreview(
            avatar = oconGroupAvatar,
            name = "Cool Doods",
            message = "wtf is this ocon",
            timestamp = 30.minutes.ago,
            delivery = DeliveryState.Sent,
        ),
        sentCommunityChatPreview(
            avatar = oconGroupAvatar,
            name = "Awesome Doods",
            message = "hello? what is this?",
            timestamp = 45.minutes.ago,
            delivery = DeliveryState.Sent,
        ),
        receivedCommunityChatPreview(
            avatar = oconGroupAvatar,
            name = "Awesome Doods",
            message = "Esteban, what is this? 🦅🦅",
            timestamp = 48.minutes.ago,
            sender = SingleAvatarState(
                url = null,
                placeholder = "logie_bear",
            ),
        ),
        ChatPreviewState(
            avatar = oconGroupAvatar,
            name = "Awesome Doods",
            content = null,
        ),
        ChatPreviewState(
            avatar = oconGroupAvatar,
            name = "Awesome Doods",
            content = null,
        ),
        ChatPreviewState(
            avatar = oconGroupAvatar,
            name = "Awesome Doods",
            content = null,
        ),
    ).sortedByDescending { it.content?.timestamp }

    val ChatPreviewsState = ChatPreviewsState(
        nudgedConversations = NudgedConversations,
        unreadConversations = UnreadConversations,
        readConversations = ReadConversations,
    )

    private fun sentDirectChatPreview(
        name: String,
        message: String,
        timestamp: Instant,
        delivery: DeliveryState,
        emphasized: Boolean = false,
    ) = chatPreview(
        avatar = SingleAvatarState(
            url = null,
            placeholder = name,
        ),
        name = name,
        message = message,
        timestamp = timestamp,
        delivery = delivery,
        sender = SentState,
        emphasized = emphasized,
    )

    private fun receivedDirectChatPreview(
        name: String,
        message: String,
        timestamp: Instant,
        emphasized: Boolean = false,
    ) = chatPreview(
        avatar = SingleAvatarState(
            url = null,
            placeholder = name,
        ),
        name = name,
        message = message,
        timestamp = timestamp,
        delivery = null,
        sender = ReceivedDirectState,
        emphasized = emphasized,
    )

    private fun sentCommunityChatPreview(
        avatar: AvatarState,
        name: String,
        message: String,
        timestamp: Instant,
        delivery: DeliveryState,
        emphasized: Boolean = false,
    ) = chatPreview(
        avatar = avatar,
        name = name,
        message = message,
        timestamp = timestamp,
        delivery = delivery,
        sender = SentState,
        emphasized = emphasized,
    )

    private fun receivedCommunityChatPreview(
        avatar: AvatarState,
        name: String,
        message: String,
        timestamp: Instant,
        sender: SingleAvatarState,
        emphasized: Boolean = false,
    ) = chatPreview(
        avatar = avatar,
        name = name,
        message = message,
        timestamp = timestamp,
        delivery = null,
        sender = ReceivedCommunityState(sender),
        emphasized = emphasized,
    )

    private fun chatPreview(
        avatar: AvatarState,
        name: String,
        message: String,
        timestamp: Instant,
        delivery: DeliveryState?,
        sender: ChatPreviewSenderState,
        emphasized: Boolean,
    ) = ChatPreviewState(
        avatar = avatar,
        name = name,
        content = ChatPreviewContentState(
            message = message,
            timestamp = timestamp.toLocalDateTime(),
            delivery = delivery,
            sender = sender,
        ),
        emphasized = emphasized,
    )
}