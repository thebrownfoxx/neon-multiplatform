package com.thebrownfoxx.neon.server.repository.data

import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.repository.data.model.ChatGroupRecord
import kotlinx.datetime.Instant

typealias DirectConversationBuilder = DirectConversationBuilderScope.() -> Unit

class DirectConversationBuilderScope internal constructor() {
    private val memberIds = mutableSetOf<MemberId>()
    private val messages = mutableListOf<ConversationBuilderScope.() -> Unit>()

    fun MemberId.said(
        content: String,
        timestamp: Instant,
        delivery: Delivery = Delivery.Read,
    ) {
        memberIds.add(this)
        messages.add {
            said(content, timestamp, delivery)
        }
    }

    fun member(id: MemberId): MemberId {
        memberIds.add(id)
        return id
    }

    internal fun build(): DirectConversationRecord {
        val chatGroupRecord = ChatGroupRecord(
            group = ChatGroup(),
            memberIds = memberIds,
        )

        val conversationBuilderScope = ConversationBuilderScope(chatGroupRecord.group.id).apply {
            for (message in messages) message()
        }

        return DirectConversationRecord(chatGroupRecord, conversationBuilderScope.build())
    }
}

internal data class DirectConversationRecord(
    val chatGroupRecord: ChatGroupRecord,
    val messages: List<Message>,
)