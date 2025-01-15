package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.GroupMemberRepository
import com.thebrownfoxx.neon.client.repository.GroupRepository
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.GroupManager.GetGroupError
import com.thebrownfoxx.neon.client.service.GroupManager.GetMembersError
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.extension.failedWith
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.mapError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstGroupManager(
    private val remoteGroupManager: GroupManager,
    private val localGroupRepository: GroupRepository,
    private val localGroupMemberRepository: GroupMemberRepository,
    externalScope: CoroutineScope,
) : GroupManager {
    private val getGroupCache = Cache<GroupId, Outcome<LocalGroup, GetGroupError>>(externalScope)
    private val getMembersCache =
        Cache<GroupId, Outcome<Set<MemberId>, GetMembersError>>(externalScope)

    override fun getGroup(id: GroupId): Flow<Outcome<LocalGroup, GetGroupError>> {
        return getGroupCache.getOrInitialize(id) {
            val mappedLocalFlow = localGroupRepository.getAsFlow(id).map { local ->
                local.mapError { it.toGetGroupError() }
            }
            offlineFirst(
                localFlow = mappedLocalFlow,
                remoteFlow = remoteGroupManager.getGroup(id),
            ) {
                defaultTransform(
                    succeeded = { it is Success },
                    notFound = { it.failedWith(GetGroupError.NotFound) },
                    failedUnexpectedly = { it.failedWith(GetGroupError.UnexpectedError) },
                    updateLocal = { updateGroup(it) },
                    deleteLocal = { TODO() },
                )
            }
        }
    }

    override fun getMembers(groupId: GroupId): Flow<Outcome<Set<MemberId>, GetMembersError>> {
        return getMembersCache.getOrInitialize(groupId) {
            val mappedLocalFlow =
                localGroupMemberRepository.getMembersAsFlow(groupId).map { local ->
                    local.mapError { GetMembersError.UnexpectedError }
                }
            offlineFirst(
                localFlow = mappedLocalFlow,
                remoteFlow = remoteGroupManager.getMembers(groupId),
            ) {
                defaultTransform(
                    localSucceeded = { it is Success && it.value.isNotEmpty() },
                    localNotFound = { it is Success && it.value.isEmpty() },
                    localFailedUnexpectedly = { it.failedWith(GetMembersError.UnexpectedError) },
                    remoteSucceeded = { it is Success },
                    remoteNotFound = { it.failedWith(GetMembersError.GroupNotFound) },
                    remoteFailedUnexpectedly = { it.failedWith(GetMembersError.UnexpectedError) },
                    updateLocal = { updateMemberIds(groupId, it) },
                    deleteLocal = { TODO() },
                )
            }
        }
    }

    private fun GetError.toGetGroupError() = when (this) {
        GetError.NotFound -> GetGroupError.NotFound
        GetError.ConnectionError, GetError.UnexpectedError ->
            GetGroupError.UnexpectedError
    }

    private suspend fun updateGroup(groupOutcome: Outcome<LocalGroup, *>) {
        val group = groupOutcome.getOrElse { return }
        localGroupRepository.upsert(group)
    }

    private suspend fun updateMemberIds(
        groupId: GroupId,
        memberIdsOutcome: Outcome<Set<MemberId>, *>,
    ) {
        val memberIds = memberIdsOutcome.getOrElse { return }
        localGroupMemberRepository.batchUpsert(groupId, memberIds)
    }
}