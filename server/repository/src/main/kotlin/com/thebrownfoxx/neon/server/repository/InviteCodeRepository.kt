package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import kotlinx.coroutines.flow.Flow

interface InviteCodeRepository {
    fun get(groupId: GroupId): Flow<Outcome<String, GetError>>
    suspend fun getGroup(inviteCode: String): Outcome<GroupId, GetError>
    suspend fun set(groupId: GroupId, inviteCode: String): UnitOutcome<RepositorySetInviteCodeError>
}

enum class RepositorySetInviteCodeError {
    DuplicateInviteCode,
    ConnectionError,
}