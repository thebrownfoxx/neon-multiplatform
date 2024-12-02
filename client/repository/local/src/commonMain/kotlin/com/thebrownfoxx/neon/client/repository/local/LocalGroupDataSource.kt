package com.thebrownfoxx.neon.client.repository.local

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.UnitOutcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import kotlinx.coroutines.flow.Flow

interface LocalGroupDataSource {
    fun getAsFlow(id: GroupId): Flow<Outcome<LocalGroup, GetError>>
    suspend fun upsert(group: LocalGroup): UnitOutcome<ConnectionError>
}