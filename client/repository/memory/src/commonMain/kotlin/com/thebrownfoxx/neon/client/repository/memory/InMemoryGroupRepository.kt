package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.GroupRepository
import com.thebrownfoxx.neon.client.repository.model.AddEntityError
import com.thebrownfoxx.neon.client.repository.model.AddEntityResult
import com.thebrownfoxx.neon.client.repository.model.GetEntityError
import com.thebrownfoxx.neon.client.repository.model.GetEntityResult
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InMemoryGroupRepository : GroupRepository {
    private val groups = mutableMapOf<GroupId, Group>()

    override fun get(id: GroupId): Flow<GetEntityResult<Group>> {
        val result = when (val group = groups[id]) {
            null -> Failure(GetEntityError.NotFound)
            else -> Success(group)
        }

        return flowOf(result)
    }

    override suspend fun add(group: Group): AddEntityResult {
        return when {
            groups.containsKey(group.id) -> Failure(AddEntityError.DuplicateId)
            else -> {
                groups[group.id] = group
                UnitSuccess()
            }
        }
    }
}