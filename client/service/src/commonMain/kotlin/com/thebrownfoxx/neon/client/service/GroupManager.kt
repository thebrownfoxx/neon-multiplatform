package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface GroupManager {
    fun getGroup(id: GroupId): Flow<Outcome<LocalGroup, GetGroupError>>

    enum class GetGroupError {
        NotFound,
        UnexpectedError,
    }
}