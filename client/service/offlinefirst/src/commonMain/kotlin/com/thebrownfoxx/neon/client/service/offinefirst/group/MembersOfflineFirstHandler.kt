package com.thebrownfoxx.neon.client.service.offinefirst.group

import com.thebrownfoxx.neon.client.repository.GroupMemberRepository
import com.thebrownfoxx.neon.client.service.GroupManager.GetMembersError
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstHandler
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.mapError

class MembersOfflineFirstHandler(
    private val groupId: GroupId,
    private val localGroupMemberRepository: GroupMemberRepository,
) : OfflineFirstHandler<RepositoryMembers, ServiceMembers, ServiceMembers> {
    override fun mapLocal(local: RepositoryMembers): ServiceMembers {
        return local.mapError { it.toGetMembersError() }
    }

    override fun hasLocalFailed(local: RepositoryMembers): Boolean {
        return local !is Success || local.value.isEmpty()
    }

    override suspend fun updateLocal(newRemote: ServiceMembers, oldLocal: RepositoryMembers) {
        when (newRemote) {
            is Failure -> onRemoteFailure(newRemote.error, oldLocal)
            is Success -> onRemoteSuccess(newRemote.value, oldLocal)
        }
    }

    private fun onRemoteFailure(
        remoteError: GetMembersError,
        oldLocal: RepositoryMembers,
    ) {
        if (remoteError != GetMembersError.GroupNotFound || oldLocal !is Success) return
        TODO("Delete ${oldLocal.value}")
    }

    private suspend fun onRemoteSuccess(
        remoteMembers: Set<MemberId>,
        oldLocal: RepositoryMembers,
    ) {
        localGroupMemberRepository.batchUpsert(groupId, remoteMembers)
        if (oldLocal !is Success) return
        val removedMembers = oldLocal.value.filter { it !in remoteMembers }
        if (removedMembers.isNotEmpty()) TODO("Remove $removedMembers")
    }

    private fun DataOperationError.toGetMembersError() = when (this) {
        DataOperationError.ConnectionError, DataOperationError.UnexpectedError ->
            GetMembersError.UnexpectedError
    }
}

private typealias RepositoryMembers = Outcome<Set<MemberId>, DataOperationError>
private typealias ServiceMembers = Outcome<Set<MemberId>, GetMembersError>