package com.thebrownfoxx.neon.server.repository.memory

import com.thebrownfoxx.neon.common.annotation.TestApi
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.common.model.unitSuccess
import com.thebrownfoxx.neon.server.repository.group.GroupRepository
import com.thebrownfoxx.neon.server.repository.group.model.AddGroupError
import com.thebrownfoxx.neon.server.repository.group.model.GetGroupError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryGroupRepository : GroupRepository {
    private val groups = MutableStateFlow<Map<GroupId, Group>>(emptyMap())

    @TestApi
    val groupList get() = groups.value.map { it.value }

    override fun get(id: GroupId): Flow<Result<Group, GetGroupError>> {
        return groups.mapLatest { groups ->
            when (val group = groups[id]) {
                null -> Failure(GetGroupError.NotFound)
                else -> Success(group)
            }
        }
    }

    override suspend fun add(group: Group): UnitResult<AddGroupError> {
        return when {
            groups.value.containsKey(group.id) -> Failure(AddGroupError.DuplicateId)
            else -> {
                groups.update { it + (group.id to group) }
                unitSuccess()
            }
        }
    }
}