package com.thebrownfoxx.neon.client.service.offinefirst.group

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.LocalGroupMemberRepository
import com.thebrownfoxx.neon.client.repository.LocalGroupRepository
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.GroupManager.GetGroupError
import com.thebrownfoxx.neon.client.service.GroupManager.GetMembersError
import com.thebrownfoxx.neon.client.service.offinefirst.offlineFirstFlow
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.extension.flow.mirrorTo
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.map.mapError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class OfflineFirstGroupManager(
    private val remoteGroupManager: GroupManager,
    private val localGroupRepository: LocalGroupRepository,
    private val localGroupMemberRepository: LocalGroupMemberRepository,
    externalScope: CoroutineScope,
) : GroupManager {
    private val groupCache = Cache<GroupId, Outcome<LocalGroup, GetGroupError>>(externalScope)
    private val membersCache =
        Cache<GroupId, Outcome<Set<MemberId>, GetMembersError>>(externalScope)

    override fun getGroup(id: GroupId): Flow<Outcome<LocalGroup, GetGroupError>> {
        return groupCache.getOrInitialize(id) {
            offlineFirstFlow(
                localFlow = localGroupRepository.getAsFlow(id),
                remoteFlow = remoteGroupManager.getGroup(id),
                handler = GroupOfflineFirstHandler(localGroupRepository),
            ).mirrorTo(this) { groupOutcome ->
                groupOutcome.mapError { it.toGetGroupError() }
            }
        }
    }

    override fun getMembers(groupId: GroupId): Flow<Outcome<Set<MemberId>, GetMembersError>> {
        return membersCache.getOrInitialize(groupId) {
            offlineFirstFlow(
                localFlow = localGroupMemberRepository.getMembersAsFlow(groupId),
                remoteFlow = remoteGroupManager.getMembers(groupId),
                handler = MembersOfflineFirstHandler(groupId, localGroupMemberRepository),
            ).mirrorTo(this) { membersOutcome ->
                membersOutcome.mapError { it.toGetMembersError() }
            }
        }
    }

    private fun GetError.toGetGroupError() = when (this) {
        GetError.NotFound -> GetGroupError.NotFound
        GetError.ConnectionError, GetError.UnexpectedError -> GetGroupError.UnexpectedError
    }

    private fun DataOperationError.toGetMembersError() = when (this) {
        DataOperationError.ConnectionError, DataOperationError.UnexpectedError ->
            GetMembersError.UnexpectedError
    }
}