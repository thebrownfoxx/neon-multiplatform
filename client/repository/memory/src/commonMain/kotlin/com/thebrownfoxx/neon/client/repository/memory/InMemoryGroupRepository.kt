package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.group.GroupRepository
import com.thebrownfoxx.neon.client.repository.group.model.AddGroupEntityError
import com.thebrownfoxx.neon.client.repository.group.model.AddGroupMemberEntityError
import com.thebrownfoxx.neon.client.repository.group.model.GetGroupEntityError
import com.thebrownfoxx.neon.client.repository.group.model.GetGroupMemberEntitiesError
import com.thebrownfoxx.neon.client.repository.group.model.InGodCommunityError
import com.thebrownfoxx.neon.common.annotation.TestApi
import com.thebrownfoxx.neon.common.model.Community
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

    override fun get(id: GroupId): Flow<Result<Group, GetGroupEntityError>> {
        return inMemoryGroups.mapLatest { inMemoryGroups ->
            when (val inMemoryGroup = inMemoryGroups[id]) {
                null -> Failure(GetGroupEntityError.NotFound)
                else -> Success(inMemoryGroup.group)
            }
        }
    }

    override fun getMembers(id: GroupId): Flow<Result<Set<MemberId>, GetGroupMemberEntitiesError>> {
        return inMemoryGroups.mapLatest { groups ->
            when (val inMemoryGroup = groups[id]) {
                null -> Failure(GetGroupMemberEntitiesError.GroupNotFound)
                else -> Success(inMemoryGroup.members)
            }
        }
    }

    override fun inGodCommunity(memberId: MemberId): Flow<Result<Boolean, InGodCommunityError>> {
        return inMemoryGroups.mapLatest {
            val inGodGroup = it.values.any { inMemoryGroup ->
                inMemoryGroup.group is Community &&
                        inMemoryGroup.group.god &&
                        memberId in inMemoryGroup.members
            }

            Success(inGodGroup)
        }
    }

    override suspend fun add(group: Group): UnitResult<AddGroupEntityError> {
        return when {
            inMemoryGroups.value.containsKey(group.id) -> Failure(AddGroupEntityError.DuplicateId)
            else -> {
                inMemoryGroups.update { it + (group.id to InMemoryGroup(group, emptySet())) }
                unitSuccess()
            }
        }
    }

    override suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
    ): UnitResult<AddGroupMemberEntityError> {
        return when {
            !inMemoryGroups.value.containsKey(groupId) -> Failure(AddGroupMemberEntityError.GroupNotFound)
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