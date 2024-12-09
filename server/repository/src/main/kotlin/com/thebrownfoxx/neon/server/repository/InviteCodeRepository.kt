package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface InviteCodeRepository {
    fun getAsFlow(groupId: GroupId): Flow<Outcome<InviteCode, GetError>>
    suspend fun getGroup(inviteCode: String): Outcome<GroupId, GetError>
    suspend fun set(
        groupId: GroupId,
        inviteCode: String,
    ): ReversibleUnitOutcome<SetInviteCodeError>

    enum class SetInviteCodeError {
        DuplicateInviteCode,
        ConnectionError,
        UnexpectedError,
    }
}

typealias InviteCode = String