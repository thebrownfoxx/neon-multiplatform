package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.GroupRepository
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.GroupManager.GetGroupError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.memberBlockContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultGroupManager(private val groupRepository: GroupRepository) : GroupManager {
    override fun getGroup(id: GroupId): Flow<Outcome<LocalGroup, GetGroupError>> {
        memberBlockContext("getGroup") {
            return groupRepository.get(id).map { outcome ->
                outcome.mapError { error ->
                    when (error) {
                        GetError.NotFound -> GetGroupError.NotFound
                        GetError.ConnectionError, GetError.UnexpectedError ->
                            GetGroupError.UnexpectedError
                    }
                }
            }
        }
    }
}