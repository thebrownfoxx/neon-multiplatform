package com.thebrownfoxx.neon.client.repository.remote

import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Member
import kotlinx.coroutines.flow.Flow

interface RemoteMemberDataSource {
    fun getAsFlow(id: MemberId): Flow<Outcome<Member, GetMemberError>>
}

enum class GetMemberError {
    NotFound,
    ServerError,
}