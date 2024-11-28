package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Member
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    fun getAsFlow(id: MemberId): Flow<Outcome<Member, GetError>>
    suspend fun get(id: MemberId): Outcome<Member, GetError>
    suspend fun getId(username: String): Outcome<MemberId, GetError>
    suspend fun add(member: Member): ReversibleUnitOutcome<RepositoryAddMemberError>
}

enum class RepositoryAddMemberError {
    DuplicateId,
    DuplicateUsername,
    ConnectionError,
}