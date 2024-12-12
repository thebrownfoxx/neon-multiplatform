package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface MemberManager {
    fun getMember(id: MemberId): Flow<Outcome<LocalMember, GetMemberError>>

    enum class GetMemberError {
        NotFound,
        UnexpectedError,
    }
}