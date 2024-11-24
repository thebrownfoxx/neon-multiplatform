package com.thebrownfoxx.neon.server.service.member

import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.neon.server.service.member.model.GetMemberError
import com.thebrownfoxx.neon.server.service.member.model.RegisterMemberError
import kotlinx.coroutines.flow.Flow

interface MemberManager {
    fun getMember(id: MemberId): Flow<Outcome<Member, GetMemberError>>

    suspend fun registerMember(
        inviteCode: String,
        username: String,
        password: String,
    ): Outcome<MemberId, RegisterMemberError>
}