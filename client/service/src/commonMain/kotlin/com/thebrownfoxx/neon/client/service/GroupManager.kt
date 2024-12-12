package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface GroupManager {
    fun getGroup(id: GroupId): Flow<Outcome<LocalGroup, GetGroupError>>

    fun getMembers(groupId: GroupId): Flow<Outcome<Set<MemberId>, UnexpectedGetMembersError>>

    enum class GetGroupError {
        NotFound,
        UnexpectedError,
    }

    data object UnexpectedGetMembersError
}