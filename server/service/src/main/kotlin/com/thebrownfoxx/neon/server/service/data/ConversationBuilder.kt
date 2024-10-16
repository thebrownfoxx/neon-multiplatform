package com.thebrownfoxx.neon.server.service.data

import com.thebrownfoxx.neon.common.model.Delivery
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.model.MessageId
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