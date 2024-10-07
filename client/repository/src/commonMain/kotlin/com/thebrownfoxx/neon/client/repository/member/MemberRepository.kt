package com.thebrownfoxx.neon.client.repository.member

import com.thebrownfoxx.neon.client.repository.member.model.AddMemberError
import com.thebrownfoxx.neon.client.repository.member.model.GetMemberError
import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    fun get(id: MemberId): Flow<Result<Member, GetMemberError>>
    suspend fun add(member: Member): UnitResult<AddMemberError>
}