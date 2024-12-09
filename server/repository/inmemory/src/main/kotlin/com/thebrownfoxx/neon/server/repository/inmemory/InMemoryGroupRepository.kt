package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.server.model.Group
import com.thebrownfoxx.neon.server.repository.GroupRepository
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.memberBlockContext
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
        memberBlockContext("getAsFlow") {
            return groups.mapLatest { groups ->
                when (val group = groups[id]) {
                    null -> Failure(GetError.NotFound)
                    else -> Success(group)
                }
            }
        }
    }

    override suspend fun get(id: GroupId): Outcome<Group, GetError> {
        return getAsFlow(id).first()
    }

    override suspend fun add(group: Group): ReversibleUnitOutcome<AddError> {
        memberBlockContext("add") {
            return when {
                groups.value.containsKey(group.id) -> Failure(AddError.Duplicate)
                else -> {
                    groups.update { it + (group.id to group) }
                    UnitSuccess
                }
            }.asReversible { groups.update { it - group.id } }
        }
    }
}