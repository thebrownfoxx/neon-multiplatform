package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.common.annotation.TestApi
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.common.model.unitSuccess
import com.thebrownfoxx.neon.server.repository.group.GroupRepository
import com.thebrownfoxx.neon.server.repository.group.model.RepositoryAddGroupError
import com.thebrownfoxx.neon.server.repository.group.model.RepositoryGetGroupError
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

    override fun get(id: GroupId): Flow<Result<Group, RepositoryGetGroupError>> {
        return groups.mapLatest { groups ->
            when (val group = groups[id]) {
                null -> Failure(RepositoryGetGroupError.NotFound)
                else -> Success(group)
            }
        }
    }

    override suspend fun add(group: Group): UnitResult<RepositoryAddGroupError> {
        return when {
            groups.value.containsKey(group.id) -> Failure(RepositoryAddGroupError.DuplicateId)
            else -> {
                groups.update { it + (group.id to group) }
                unitSuccess()
            }
        }
    }
}