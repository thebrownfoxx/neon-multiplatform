package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.group.GroupRepository
import com.thebrownfoxx.neon.client.repository.group.model.RepositoryGetGroupError
import com.thebrownfoxx.neon.client.service.group.GroupManager
import com.thebrownfoxx.neon.client.service.group.model.GetGroupError
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.mapError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultGroupManager(private val groupRepository: GroupRepository) : GroupManager {
    override fun getGroup(id: GroupId): Flow<Outcome<LocalGroup, GetGroupError>> {
        return groupRepository.get(id).map { outcome ->
            outcome.mapError { error ->
                when (error) {
                    RepositoryGetGroupError.NotFound -> GetGroupError.NotFound
                    RepositoryGetGroupError.ConnectionError -> GetGroupError.ConnectionError
                }
            }
        }
    }
}