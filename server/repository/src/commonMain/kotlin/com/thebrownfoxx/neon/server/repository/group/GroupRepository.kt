package com.thebrownfoxx.neon.server.repository.group

import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.repository.group.model.AddGroupError
import com.thebrownfoxx.neon.server.repository.group.model.GetGroupError
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun get(id: GroupId): Flow<Result<Group, GetGroupError>>
    suspend fun add(group: Group): UnitResult<AddGroupError>
}