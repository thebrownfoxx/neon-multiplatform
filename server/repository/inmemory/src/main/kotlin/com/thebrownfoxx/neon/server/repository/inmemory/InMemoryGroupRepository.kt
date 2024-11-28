package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.unitSuccess
import com.thebrownfoxx.neon.server.model.Group
import com.thebrownfoxx.neon.server.repository.GroupRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryGroupRepository : GroupRepository {
    private val groups = MutableStateFlow<Map<GroupId, Group>>(emptyMap())

    override fun getAsFlow(id: GroupId): Flow<Outcome<Group, GetError>> {
        return groups.mapLatest { groups ->
            when (val group = groups[id]) {
                null -> Failure(GetError.NotFound)
                else -> Success(group)
            }
        }
    }

    override suspend fun get(id: GroupId): Outcome<Group, GetError> {
        return getAsFlow(id).first()
    }

    override suspend fun add(group: Group): ReversibleUnitOutcome<AddError> {
        return when {
            groups.value.containsKey(group.id) -> Failure(AddError.Duplicate)
            else -> {
                groups.update { it + (group.id to group) }
                unitSuccess()
            }
        }.asReversible { groups.update { it - group.id } }
    }
}