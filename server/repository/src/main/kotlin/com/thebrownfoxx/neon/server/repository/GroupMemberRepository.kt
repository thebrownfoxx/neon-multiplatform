package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface GroupMemberRepository {
    fun getMembersAsFlow(groupId: GroupId): Flow<Outcome<List<MemberId>, DataOperationError>>
    fun getGroupsAsFlow(memberId: MemberId): Flow<Outcome<List<GroupId>, DataOperationError>>
    fun getAdminsAsFlow(groupId: GroupId): Flow<Outcome<List<MemberId>, DataOperationError>>

    suspend fun getMembers(groupId: GroupId): Outcome<List<MemberId>, DataOperationError>
    suspend fun getGroups(memberId: MemberId): Outcome<List<GroupId>, DataOperationError>
    suspend fun getAdmins(groupId: GroupId): Outcome<List<MemberId>, DataOperationError>

    suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean = false,
    ): ReversibleUnitOutcome<AddError>
}