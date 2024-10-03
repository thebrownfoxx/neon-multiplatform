package com.thebrownfoxx.neon.client.repository

import com.thebrownfoxx.neon.client.repository.model.AddEntityResult
import com.thebrownfoxx.neon.client.repository.model.GetEntityResult
import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.GroupId
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun get(id: GroupId): Flow<GetEntityResult<Group>>
    suspend fun add(group: Group): AddEntityResult
}