package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun get(id: MessageId): Flow<Outcome<Message, GetError>>

    suspend fun add(message: Message): UnitOutcome<AddError>

    suspend fun update(message: Message): UnitOutcome<UpdateError>

    suspend fun getConversations(
        memberId: MemberId,
        count: Int,
        offset: Int,
        read: Boolean? = null,
        descending: Boolean = false,
    ): Outcome<Set<GroupId>, ConnectionError>

    fun getConversationCount(
        memberId: MemberId,
        read: Boolean? = null,
    ): Flow<Outcome<Int, ConnectionError>>

    fun getConversationPreview(
        id: GroupId,
    ): Flow<Outcome<MessageId?, ConnectionError>>

    fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Flow<Outcome<Set<MessageId>, ConnectionError>>

    fun getUnreadMessages(
        groupId: GroupId,
    ): Flow<Outcome<Set<MessageId>, ConnectionError>>
}