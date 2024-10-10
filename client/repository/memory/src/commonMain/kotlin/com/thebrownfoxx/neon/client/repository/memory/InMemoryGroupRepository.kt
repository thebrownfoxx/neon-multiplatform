package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.group.GroupRepository
import com.thebrownfoxx.neon.client.repository.group.model.AddGroupError
import com.thebrownfoxx.neon.client.repository.group.model.AddGroupMemberError
import com.thebrownfoxx.neon.client.repository.group.model.GetGroupError
import com.thebrownfoxx.neon.client.repository.group.model.GetGroupMembersError
import com.thebrownfoxx.neon.common.annotation.TestApi
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.common.model.unitSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryGroupRepository : GroupRepository {
    private val inMemoryGroups = MutableStateFlow<Map<GroupId, InMemoryGroup>>(emptyMap())

    @TestApi
    val groupList get() = inMemoryGroups.value.map { it.value.group }

    override fun get(id: GroupId): Flow<Result<Group, GetGroupError>> {
        return inMemoryGroups.mapLatest { inMemoryGroups ->
            when (val inMemoryGroup = inMemoryGroups[id]) {
                null -> Failure(GetGroupError.NotFound)
                else -> Success(inMemoryGroup.group)
            }
        }
    }

    override fun getMembers(id: GroupId): Flow<Result<Set<MemberId>, GetGroupMembersError>> {
        return inMemoryGroups.mapLatest { groups ->
            when (val inMemoryGroup = groups[id]) {
                null -> Failure(GetGroupMembersError.GroupNotFound)
                else -> Success(inMemoryGroup.members)
            }
        }
    }

    override suspend fun add(group: Group): UnitResult<AddGroupError> {
        return when {
            inMemoryGroups.value.containsKey(group.id) -> Failure(AddGroupError.DuplicateId)
            else -> {
                inMemoryGroups.update { it + (group.id to InMemoryGroup(group, emptySet())) }
                unitSuccess()
            }
        }
    }

    override suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
    ): UnitResult<AddGroupMemberError> {
        return when {
            !inMemoryGroups.value.containsKey(groupId) -> Failure(AddGroupMemberError.GroupNotFound)
            else -> {
                inMemoryGroups.update {
                    val inMemoryGroup = it[groupId]
                    val newGroup = inMemoryGroup?.copy(members = inMemoryGroup.members + memberId)

                    when (newGroup) {
                        null -> it
                        else -> it.toMutableMap().apply { this[groupId] = newGroup }
                    }
                }
                unitSuccess()
            }
        }
    }
}

data class InMemoryGroup(
    val group: Group,
    val members: Set<MemberId>,
)