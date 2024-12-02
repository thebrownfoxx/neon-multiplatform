package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.server.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getAsFlow(id: GroupId): Flow<Outcome<Group, GetError>>
    suspend fun get(id: GroupId): Outcome<Group, GetError>
    suspend fun add(group: Group): ReversibleUnitOutcome<AddError>
}