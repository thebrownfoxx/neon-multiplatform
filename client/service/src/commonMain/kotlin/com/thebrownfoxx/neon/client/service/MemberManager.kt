package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import kotlinx.coroutines.flow.Flow

interface MemberManager {
    fun get(id: MemberId): Flow<Member>
    suspend fun register(member: Member, password: String)
}