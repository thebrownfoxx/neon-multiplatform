package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import kotlinx.coroutines.flow.Flow

interface GroupMemberRepository {
    fun getMembersAsFlow(groupId: GroupId): Flow<Outcome<List<MemberId>, ConnectionError>>
    fun getGroupsAsFlow(memberId: MemberId): Flow<Outcome<List<GroupId>, ConnectionError>>
    fun getAdminsAsFlow(groupId: GroupId): Flow<Outcome<List<MemberId>, ConnectionError>>

    suspend fun getMembers(groupId: GroupId): Outcome<List<MemberId>, ConnectionError>
    suspend fun getGroups(memberId: MemberId): Outcome<List<GroupId>, ConnectionError>
    suspend fun getAdmins(groupId: GroupId): Outcome<List<MemberId>, ConnectionError>

    suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean = false,
    ): ReversibleUnitOutcome<AddError>
}