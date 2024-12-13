package com.thebrownfoxx.neon.client.repository.remote

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface RemoteGroupMemberDataSource {
    fun getMembersAsFlow(groupId: GroupId): Flow<Outcome<Set<MemberId>, GetMembersError>>

    // TODO: Omg, idk how to properly represent errors
    enum class GetMembersError {
        GroupNotFound,
        UnexpectedError,
    }
}