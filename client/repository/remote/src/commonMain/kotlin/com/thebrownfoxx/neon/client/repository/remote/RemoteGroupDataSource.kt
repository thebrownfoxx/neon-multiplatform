package com.thebrownfoxx.neon.client.repository.remote

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.server.model.Group
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface RemoteGroupDataSource {
    fun getAsFlow(id: GroupId): Flow<Outcome<Group, GetError>>
}