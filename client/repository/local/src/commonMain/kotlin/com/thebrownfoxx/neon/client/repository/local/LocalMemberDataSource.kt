package com.thebrownfoxx.neon.client.repository.local

import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface LocalMemberDataSource {
    fun getAsFlow(id: MemberId): Flow<Outcome<LocalMember, GetError>>
    suspend fun upsert(member: LocalMember): UnitOutcome<DataOperationError>
}