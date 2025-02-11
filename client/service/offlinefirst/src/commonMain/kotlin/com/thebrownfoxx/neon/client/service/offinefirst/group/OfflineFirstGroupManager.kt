package com.thebrownfoxx.neon.client.service.offinefirst.group

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.remote.RemoteGroupManager
import com.thebrownfoxx.neon.client.repository.LocalGroupMemberRepository
import com.thebrownfoxx.neon.client.repository.LocalGroupRepository
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.GroupManager.GetGroupError
import com.thebrownfoxx.neon.client.service.GroupManager.GetMembersError
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstProvider
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.map.mapError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class OfflineFirstGroupManager(
    private val remoteGroupManager: RemoteGroupManager,
    private val localGroupRepository: LocalGroupRepository,
    private val localGroupMemberRepository: LocalGroupMemberRepository,
    private val externalScope: CoroutineScope,
) : GroupManager {
    private val groupCache = groupCache(externalScope)
    private val membersCache = createMembersCache(externalScope)

    override fun getGroup(id: GroupId): Flow<Outcome<LocalGroup, GetGroupError>> {
        return groupCache.getOrPut(id) {
            OfflineFirstProvider(
                localFlow = localGroupRepository.getAsFlow(id),
                remoteFlow = remoteGroupManager.getGroup(id),
                handler = GroupOfflineFirstHandler(localGroupRepository),
                externalScope = externalScope,
            )
        }.getAsMappedFlow { groupOutcome ->
            groupOutcome.mapError { it.toGetGroupError() }
        }
    }

    override fun getMembers(groupId: GroupId): Flow<Outcome<Set<MemberId>, GetMembersError>> {
        return membersCache.getOrPut(groupId) {
            OfflineFirstProvider(
                localFlow = localGroupMemberRepository.getMembersAsFlow(groupId),
                remoteFlow = remoteGroupManager.getMembers(groupId),
                handler = MembersOfflineFirstHandler(groupId, localGroupMemberRepository),
                externalScope = externalScope,
            )
        }.getAsMappedFlow { memberOutcome ->
            memberOutcome.mapError { it.toGetMembersError() }
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