package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.GroupMemberRepository
import com.thebrownfoxx.neon.client.repository.GroupMemberRepository.LocalGroupMember
import com.thebrownfoxx.neon.client.repository.GroupRepository
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.GroupManager.GetGroupError
import com.thebrownfoxx.neon.client.service.GroupManager.GetMembersError
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.extension.mirrorTo
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class OfflineFirstGroupManager(
    private val remoteGroupManager: GroupManager,
    private val localGroupRepository: GroupRepository,
    private val localGroupMemberRepository: GroupMemberRepository,
    externalScope: CoroutineScope,
) : GroupManager {
    private val groupCache = Cache<GroupId, Outcome<LocalGroup, GetGroupError>>(externalScope)
    private val memberCache = Cache<GroupId, Outcome<Set<MemberId>, GetMembersError>>(externalScope)

    override fun getGroup(id: GroupId): Flow<Outcome<LocalGroup, GetGroupError>> {
        return groupCache.getAsFlow(id) {
            offlineFirst(
                localFlow = localGroupRepository.getAsFlow(id),
                remoteFlow = remoteGroupManager.getGroup(id),
            ) { localGroupRepository.upsert(it) }.mirrorTo(this)
        }
    }

    override fun getMembers(groupId: GroupId): Flow<Outcome<Set<MemberId>, GetMembersError>> {
        return memberCache.getAsFlow(groupId) {
            offlineFirst(
                localFlow = localGroupMemberRepository.getMembersAsFlow(groupId),
                remoteFlow = remoteGroupManager.getMembers(groupId),
            ) { memberIds ->
                val groupMembers = memberIds.map {
                    LocalGroupMember(
                        groupId = groupId,
                        memberId = it,
                        isAdmin = false,
                    )
                }
                localGroupMemberRepository.batchUpsert(groupMembers)
            }.mirrorTo(this)
        }
    }
}