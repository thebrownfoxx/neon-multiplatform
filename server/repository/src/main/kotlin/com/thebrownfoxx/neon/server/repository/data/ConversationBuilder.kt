package com.thebrownfoxx.neon.server.repository.data

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.MessageId
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message
import kotlinx.datetime.Instant

typealias ConversationBuilder = ConversationBuilderScope.() -> Unit

class ConversationBuilderScope internal constructor(private val groupId: GroupId) {
    private val messages = mutableListOf<Message>()

    fun MemberId.said(
        content: String,
        timestamp: Instant,
        delivery: Delivery = Delivery.Read,
    ) {
        val message = Message(
            id = MessageId(),
            groupId = groupId,
            senderId = this,
            content = content,
            timestamp = timestamp,
            delivery = delivery,
        )
        messages.add(message)
    }

    internal fun build() = messages.toList()
}