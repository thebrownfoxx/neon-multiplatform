package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import kotlinx.coroutines.flow.Flow

interface GroupMemberRepository {
    fun getMembers(groupId: GroupId): Flow<Outcome<List<MemberId>, ConnectionError>>
    fun getGroups(memberId: MemberId): Flow<Outcome<List<GroupId>, ConnectionError>>
    fun getAdmins(groupId: GroupId): Flow<Outcome<List<MemberId>, ConnectionError>>

    suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean = false,
    ): UnitOutcome<AddError>
}