package com.thebrownfoxx.neon.client.repository.group

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun get(id: GroupId): Flow<Outcome<LocalGroup, GetError>>
}