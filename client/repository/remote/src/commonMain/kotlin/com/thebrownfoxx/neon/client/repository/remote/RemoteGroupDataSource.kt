package com.thebrownfoxx.neon.client.repository.remote

import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.server.model.Group
import kotlinx.coroutines.flow.Flow

interface RemoteGroupDataSource {
    fun getAsFlow(id: GroupId): Flow<Outcome<Group, GetGroupError>>
}

enum class GetGroupError {
    NotFound,
    ServerError,
}