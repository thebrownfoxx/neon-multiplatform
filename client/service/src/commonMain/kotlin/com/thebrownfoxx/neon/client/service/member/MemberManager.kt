package com.thebrownfoxx.neon.client.service.member

import com.thebrownfoxx.neon.client.service.member.model.GetMemberError
import com.thebrownfoxx.neon.client.service.member.model.RegisterMemberError
import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import kotlinx.coroutines.flow.Flow

interface MemberManager {
    fun getMember(id: MemberId): Flow<Result<Member, GetMemberError>>

    suspend fun registerMember(
        inviteCode: String,
        username: String,
        password: String,
    ): UnitResult<RegisterMemberError>
}