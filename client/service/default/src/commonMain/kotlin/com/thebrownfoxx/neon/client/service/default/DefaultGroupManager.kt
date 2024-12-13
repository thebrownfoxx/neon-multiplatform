package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.GroupMemberRepository
import com.thebrownfoxx.neon.client.repository.GroupRepository
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.GroupManager.GetGroupError
import com.thebrownfoxx.neon.client.service.GroupManager.GetMembersError
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.map.mapError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultGroupManager(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
) : GroupManager {
    override fun getGroup(id: GroupId): Flow<Outcome<LocalGroup, GetGroupError>> {
        return groupRepository.getAsFlow(id).map { outcome ->
            outcome.mapError { error ->
                when (error) {
                    GetError.NotFound -> GetGroupError.NotFound
                    GetError.ConnectionError, GetError.UnexpectedError ->
                        GetGroupError.UnexpectedError
                }
            }
        }
    }

    override fun getMembers(groupId: GroupId): Flow<Outcome<Set<MemberId>, GetMembersError>> {
        return groupMemberRepository.getMembersAsFlow(groupId).map { outcome ->
            outcome.mapError { error ->
                when (error) {
                    DataOperationError.ConnectionError, DataOperationError.UnexpectedError ->
                        GetMembersError.UnexpectedError
                }
            }
        }
    }
}