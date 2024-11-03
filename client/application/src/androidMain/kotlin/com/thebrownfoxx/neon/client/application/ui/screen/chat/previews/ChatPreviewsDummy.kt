package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.GroupAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewContentState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewSenderState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ReceivedCommunityState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ReceivedDirectState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.SentState
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
            delivery = DeliveryState.Read,
        ),
        receivedDirectChatPreview(
            name = "will_joseph",
            message = "feet pics or im sabotaging your next race",
            timestamp = 2.days.ago,
            delivery = DeliveryState.Sent,
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
            delivery = DeliveryState.Sent,
            sender = SingleAvatarState(
                url = null,
                placeholder = "oscoala",
            ),
        ),
        receivedDirectChatPreview(
            name = "carlito",
            message = "you ready to get freaky? 🤤",
            timestamp = 5.minutes.ago,
            delivery = DeliveryState.Sent,
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
            delivery = DeliveryState.Sent,
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
            delivery = DeliveryState.Sent,
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


    fun sentDirectChatPreview(
        name: String,
        message: String,
        timestamp: Instant,
        delivery: DeliveryState,
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
    )

    private fun receivedDirectChatPreview(
        name: String,
        message: String,
        timestamp: Instant,
        delivery: DeliveryState,
    ) = chatPreview(
        avatar = SingleAvatarState(
            url = null,
            placeholder = name,
        ),
        name = name,
        message = message,
        timestamp = timestamp,
        delivery = delivery,
        sender = ReceivedDirectState,
    )

    private fun sentCommunityChatPreview(
        avatar: AvatarState,
        name: String,
        message: String,
        timestamp: Instant,
        delivery: DeliveryState,
    ) = chatPreview(
        avatar = avatar,
        name = name,
        message = message,
        timestamp = timestamp,
        delivery = delivery,
        sender = SentState,
    )

    private fun receivedCommunityChatPreview(
        avatar: AvatarState,
        name: String,
        message: String,
        timestamp: Instant,
        delivery: DeliveryState,
        sender: SingleAvatarState,
    ) = chatPreview(
        avatar = avatar,
        name = name,
        message = message,
        timestamp = timestamp,
        delivery = delivery,
        sender = ReceivedCommunityState(sender),
    )

    private fun chatPreview(
        avatar: AvatarState,
        name: String,
        message: String,
        timestamp: Instant,
        delivery: DeliveryState,
        sender: ChatPreviewSenderState,
    ) = ChatPreviewState(
        avatar = avatar,
        name = name,
        content = ChatPreviewContentState(
            message = message,
            timestamp = timestamp.toLocalDateTime(),
            delivery = delivery,
            sender = sender,
        )
    )
}