package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.GroupRepository
import com.thebrownfoxx.neon.client.service.group.GroupManager
import com.thebrownfoxx.neon.client.service.group.model.GetGroupError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.mapError
import com.thebrownfoxx.neon.common.type.id.GroupId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultGroupManager(private val groupRepository: GroupRepository) : GroupManager {
    override fun getGroup(id: GroupId): Flow<Outcome<LocalGroup, GetGroupError>> {
        return groupRepository.get(id).map { outcome ->
            outcome.mapError { error ->
                when (error) {
                    GetError.NotFound -> GetGroupError.NotFound
                    GetError.ConnectionError -> GetGroupError.ConnectionError
                }
            }
        }
    }
}