package com.thebrownfoxx.neon.client.repository.remote

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface RemoteMemberDataSource {
    fun getAsFlow(id: MemberId): Flow<Outcome<Member, GetError>>
}