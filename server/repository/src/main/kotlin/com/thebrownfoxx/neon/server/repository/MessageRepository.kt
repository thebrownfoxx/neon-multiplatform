package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getAsFlow(id: MessageId): Flow<Outcome<Message, GetError>>

    fun getConversationPreviewAsFlow(
        id: GroupId,
    ): Flow<Outcome<MessageId?, ConnectionError>>

    suspend fun get(id: MessageId): Outcome<Message, GetError>

    suspend fun add(message: Message): ReversibleUnitOutcome<AddError>

    suspend fun update(message: Message): ReversibleUnitOutcome<UpdateError>

    @Deprecated("Use getConversations(MemberId) instead")
    suspend fun getConversations(
        memberId: MemberId,
        count: Int,
        offset: Int,
        read: Boolean? = null,
        descending: Boolean = false,
    ): Outcome<Set<GroupId>, ConnectionError>

    suspend fun getConversations(
        memberId: MemberId,
    ): Outcome<CategorizedConversations, ConnectionError>

    @Deprecated("Use getConversations instead")
    suspend fun getConversationCount(
        memberId: MemberId,
        read: Boolean? = null,
    ): Outcome<Int, ConnectionError>

    suspend fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Outcome<Set<MessageId>, ConnectionError>

    suspend fun getUnreadMessages(
        groupId: GroupId,
    ): Outcome<Set<MessageId>, ConnectionError>
}

data class CategorizedConversations(
    val unreadGroupIds: Set<GroupId>,
    val readGroupIds: Set<GroupId>,
)