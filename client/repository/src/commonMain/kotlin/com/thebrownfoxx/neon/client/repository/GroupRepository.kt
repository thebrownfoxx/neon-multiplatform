package com.thebrownfoxx.neon.client.repository

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getAsFlow(id: GroupId): Flow<Outcome<LocalGroup, GetError>>
}