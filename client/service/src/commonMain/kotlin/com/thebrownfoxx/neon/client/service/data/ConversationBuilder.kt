package com.thebrownfoxx.neon.client.service.data

import com.thebrownfoxx.neon.common.model.Delivery
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.model.MessageId
import kotlinx.datetime.Instant

fun interface ConversationBuilder {
    fun ConversationBuilderScope.invoke()
}

class ConversationBuilderScope internal constructor(private val groupId: GroupId) {
    private val messages = mutableListOf<Message>()

    fun MemberId.said(
        content: String,
        timestamp: Instant,
        delivery: Delivery = Delivery.Read,
    ) {
        val message = Message(
            id = MessageId(),
            group = groupId,
            sender = this,
            content = content,
            timestamp = timestamp,
            delivery = delivery,
        )
        messages.add(message)
    }

    internal fun build() = Conversation(groupId, messages)
}

data class Conversation(
    val group: GroupId,
    val messages: List<Message>,
)