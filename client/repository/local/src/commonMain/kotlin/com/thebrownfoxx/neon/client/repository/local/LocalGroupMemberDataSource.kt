package com.thebrownfoxx.neon.client.repository.local

import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface LocalGroupMemberDataSource {
    fun getMembersAsFlow(groupId: GroupId): Flow<Outcome<Set<MemberId>, DataOperationError>>
    suspend fun batchUpsert(groupMembers: List<GroupMember>): UnitOutcome<DataOperationError>

    data class GroupMember(
        val groupId: GroupId,
        val memberId: MemberId,
        val isAdmin: Boolean,
    )
}