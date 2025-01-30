package com.thebrownfoxx.neon.client.repository

import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface LocalGroupMemberRepository {
    fun getMembersAsFlow(groupId: GroupId): Flow<Outcome<Set<MemberId>, DataOperationError>>

    suspend fun batchUpsert(
        groupId: GroupId,
        memberIds: Set<MemberId>,
    ): UnitOutcome<DataOperationError>
}