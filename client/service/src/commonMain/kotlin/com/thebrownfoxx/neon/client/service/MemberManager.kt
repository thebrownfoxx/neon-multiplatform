package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import kotlinx.coroutines.flow.Flow

interface MemberManager {
    fun getMember(memberId: MemberId): Flow<Member>
    suspend fun createMember(member: Member, password: String)
}