package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.outcome.unitSuccess
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryGroupMemberRepository : GroupMemberRepository {
    private val groupMembers = MutableStateFlow<Map<GroupId, List<GroupMember>>>(emptyMap())

    override fun getMembersAsFlow(
        groupId: GroupId,
    ): Flow<Outcome<List<MemberId>, ConnectionError>> {
        return groupMembers.mapLatest { groupMembers ->
            Success(groupMembers[groupId]?.map { it.id } ?: emptyList())
        }
    }

    override fun getGroupsAsFlow(
        memberId: MemberId,
    ): Flow<Outcome<List<GroupId>, ConnectionError>> {
        return groupMembers.mapLatest { groupMembers ->
            val groups = groupMembers.filter { (_, members) ->
                members.any { it.id == memberId }
            }.map { it.key }

            Success(groups)
        }
    }

    override fun getAdminsAsFlow(
        groupId: GroupId,
    ): Flow<Outcome<List<MemberId>, ConnectionError>> {
        return groupMembers.mapLatest { groupMembers ->
            val admins = groupMembers[groupId]?.filter { it.isAdmin }?.map { it.id } ?: emptyList()
            Success(admins)
        }
    }

    override suspend fun getMembers(groupId: GroupId): Outcome<List<MemberId>, ConnectionError> {
        return getMembersAsFlow(groupId).first()
    }

    override suspend fun getGroups(memberId: MemberId): Outcome<List<GroupId>, ConnectionError> {
        return getGroupsAsFlow(memberId).first()
    }

    override suspend fun getAdmins(groupId: GroupId): Outcome<List<MemberId>, ConnectionError> {
        return getAdminsAsFlow(groupId).first()
    }

    override suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean,
    ): ReversibleUnitOutcome<AddError> {
        val groupMembers = groupMembers.value[groupId] ?: emptyList()
        val duplicateMembership = groupMembers.any { it.id == memberId }

        if (duplicateMembership) return Failure(AddError.Duplicate).asReversible()

        this.groupMembers.update {
            it + (groupId to groupMembers + GroupMember(memberId, isAdmin))
        }

        return unitSuccess().asReversible {
            this.groupMembers.update { oldGroupMembers ->
                oldGroupMembers + (groupId to groupMembers.filter { it.id != memberId })
            }
        }
    }
}

private data class GroupMember(
    val id: MemberId,
    val isAdmin: Boolean,
)