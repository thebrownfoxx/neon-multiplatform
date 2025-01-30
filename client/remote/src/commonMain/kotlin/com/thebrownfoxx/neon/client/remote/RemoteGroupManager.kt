package com.thebrownfoxx.neon.client.remote

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Group
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface RemoteGroupManager {
    fun getGroup(id: GroupId): Flow<Outcome<Group, GetGroupError>>

    fun getMembers(groupId: GroupId): Flow<Outcome<Set<MemberId>, GetMembersError>>

    enum class GetGroupError {
        NotFound,
        UnexpectedError,
    }

    enum class GetMembersError {
        GroupNotFound,
        UnexpectedError,
    }
}