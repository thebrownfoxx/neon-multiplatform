package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.unitSuccess
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryGroupMemberRepository : GroupMemberRepository {
    private val groupMembers = MutableStateFlow<Map<GroupId, List<GroupMember>>>(emptyMap())

    override fun getMembers(
        groupId: GroupId,
    ): Flow<Outcome<List<MemberId>, ConnectionError>> {
        return groupMembers.mapLatest { groupMembers ->
            Success(groupMembers[groupId]?.map { it.id } ?: emptyList())
        }
    }

    override fun getGroups(
        memberId: MemberId,
    ): Flow<Outcome<List<GroupId>, ConnectionError>> {
        return groupMembers.mapLatest { groupMembers ->
            val groups = groupMembers.filter { (_, members) ->
                members.any { it.id == memberId }
            }.map { it.key }

            Success(groups)
        }
    }

    override fun getAdmins(
        groupId: GroupId,
    ): Flow<Outcome<List<MemberId>, ConnectionError>> {
        return groupMembers.mapLatest { groupMembers ->
            val admins = groupMembers[groupId]?.filter { it.isAdmin }?.map { it.id } ?: emptyList()
            Success(admins)
        }
    }

    override suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean,
    ): UnitOutcome<AddError> {
        val groupMembers = groupMembers.value[groupId] ?: emptyList()
        val duplicateMembership = groupMembers.any { it.id == memberId }

        if (duplicateMembership) return Failure(AddError.Duplicate)

        this.groupMembers.update {
            it + (groupId to groupMembers + GroupMember(memberId, isAdmin))
        }

        return unitSuccess()
    }
}

private data class GroupMember(
    val id: MemberId,
    val isAdmin: Boolean,
)