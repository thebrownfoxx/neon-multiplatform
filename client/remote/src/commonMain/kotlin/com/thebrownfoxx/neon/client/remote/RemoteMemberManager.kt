package com.thebrownfoxx.neon.client.remote

import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface RemoteMemberManager {
    fun getMember(id: MemberId): Flow<Outcome<Member, GetMemberError>>

    enum class GetMemberError {
        NotFound,
        UnexpectedError,
    }
}