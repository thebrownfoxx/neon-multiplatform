package com.thebrownfoxx.neon.client.repository

import com.thebrownfoxx.neon.client.repository.model.AddEntityResult
import com.thebrownfoxx.neon.client.repository.model.GetEntityResult
import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    fun get(id: MemberId): Flow<GetEntityResult<Member>>
    suspend fun add(member: Member): AddEntityResult
}