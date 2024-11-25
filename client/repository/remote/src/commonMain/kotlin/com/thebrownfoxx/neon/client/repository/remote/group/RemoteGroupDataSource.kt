package com.thebrownfoxx.neon.client.repository.remote.group

import com.thebrownfoxx.neon.client.repository.remote.group.model.RemoteGetGroupError
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.server.model.Group
import kotlinx.coroutines.flow.Flow

interface RemoteGroupDataSource {
    fun get(id: GroupId): Flow<Outcome<Group, RemoteGetGroupError>>
}