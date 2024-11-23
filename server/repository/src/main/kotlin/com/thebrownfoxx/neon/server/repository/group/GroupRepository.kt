package com.thebrownfoxx.neon.server.repository.group

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.model.Group
import com.thebrownfoxx.neon.server.repository.group.model.RepositoryAddGroupError
import com.thebrownfoxx.neon.server.repository.group.model.RepositoryGetGroupError
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun get(id: GroupId): Flow<Result<Group, RepositoryGetGroupError>>
    suspend fun add(group: Group): UnitResult<RepositoryAddGroupError>
}