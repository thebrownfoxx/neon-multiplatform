package com.thebrownfoxx.neon.client.repository.local.group

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.local.group.model.LocalGetGroupError
import com.thebrownfoxx.neon.client.repository.local.group.model.LocalUpsertGroupError
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import kotlinx.coroutines.flow.Flow

interface LocalGroupDataSource {
    fun get(id: GroupId): Flow<Outcome<LocalGroup, LocalGetGroupError>>
    suspend fun upsert(group: LocalGroup): UnitOutcome<LocalUpsertGroupError>
}