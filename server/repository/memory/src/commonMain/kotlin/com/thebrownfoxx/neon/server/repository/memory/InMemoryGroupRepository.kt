package com.thebrownfoxx.neon.server.repository.memory

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
import com.thebrownfoxx.neon.server.repository.group.GroupRepository
import com.thebrownfoxx.neon.server.repository.group.model.AddGroupError
import com.thebrownfoxx.neon.server.repository.group.model.AddGroupMemberError
import com.thebrownfoxx.neon.server.repository.group.model.GetGroupError
import com.thebrownfoxx.neon.server.repository.group.model.GetGroupMembersError
import com.thebrownfoxx.neon.server.repository.group.model.GetInviteCodeGroupError
import com.thebrownfoxx.neon.server.repository.group.model.InGodCommunityError
import com.thebrownfoxx.neon.server.repository.group.model.IsGroupAdminError
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

    override fun getInviteCodeGroup(inviteCode: String): Flow<Result<GroupId, GetInviteCodeGroupError>> {
        return inMemoryGroups.mapLatest { inMemoryGroups ->
            val community = inMemoryGroups.values.filterIsInstance<Community>()
                .find { it.inviteCode == inviteCode }

            when (community) {
                null -> Failure(GetInviteCodeGroupError.NotFound)
                else -> Success(community.id)
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

    override fun isGroupAdmin(
        groupId: GroupId,
        memberId: MemberId,
    ): Flow<Result<Boolean, IsGroupAdminError>> {
        return inMemoryGroups.mapLatest { inMemoryGroups ->
            when (val inMemoryGroup = inMemoryGroups[groupId]) {
                null -> Failure(IsGroupAdminError.NotFound)
                else -> Success(memberId in inMemoryGroup.admins)
            }
        }
    }

    override suspend fun add(group: Group): UnitResult<AddGroupError> {
        return when {
            inMemoryGroups.value.containsKey(group.id) -> Failure(AddGroupError.DuplicateId)
            else -> {
                inMemoryGroups.update { it + (group.id to InMemoryGroup(group)) }
                unitSuccess()
            }
        }
    }

    override suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean,
    ): UnitResult<AddGroupMemberError> {
        return when {
            !inMemoryGroups.value.containsKey(groupId) -> Failure(AddGroupMemberError.GroupNotFound)
            else -> {
                inMemoryGroups.update {
                    val inMemoryGroup = it[groupId]
                    val newGroup = inMemoryGroup?.copy(
                        members = inMemoryGroup.members + memberId,
                        admins = inMemoryGroup.admins + memberId,
                    )

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
    val members: Set<MemberId> = emptySet(),
    val admins: Set<MemberId> = emptySet(),
)