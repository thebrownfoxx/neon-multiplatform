package com.thebrownfoxx.neon.server.repository.member

import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryAddMemberError
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberError
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberIdError
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    fun get(id: MemberId): Flow<Outcome<Member, RepositoryGetMemberError>>
    fun getId(username: String): Flow<Outcome<MemberId, RepositoryGetMemberIdError>>
    suspend fun add(member: Member): UnitOutcome<RepositoryAddMemberError>
}