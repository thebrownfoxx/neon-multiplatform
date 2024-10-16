package com.thebrownfoxx.neon.server.repository.member

import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryAddMemberError
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberError
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberIdError
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    fun get(id: MemberId): Flow<Result<Member, RepositoryGetMemberError>>
    fun getId(username: String): Flow<Result<MemberId, RepositoryGetMemberIdError>>
    suspend fun add(member: Member): UnitResult<RepositoryAddMemberError>
}