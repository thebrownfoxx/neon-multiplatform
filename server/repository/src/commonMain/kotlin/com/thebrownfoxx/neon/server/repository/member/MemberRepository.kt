package com.thebrownfoxx.neon.server.repository.member

import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.repository.member.model.AddMemberError
import com.thebrownfoxx.neon.server.repository.member.model.GetMemberError
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    fun get(id: MemberId): Flow<Result<Member, GetMemberError>>
    suspend fun add(member: Member): UnitResult<AddMemberError>
}