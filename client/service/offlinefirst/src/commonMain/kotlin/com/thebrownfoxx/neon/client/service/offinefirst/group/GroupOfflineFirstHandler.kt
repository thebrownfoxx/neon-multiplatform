package com.thebrownfoxx.neon.client.service.offinefirst.group

import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.GroupRepository
import com.thebrownfoxx.neon.client.service.GroupManager.GetGroupError
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstHandler
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success

internal class GroupOfflineFirstHandler(
    private val localGroupRepository: GroupRepository,
) : OfflineFirstHandler<RepositoryGroup, ServiceGroup> {
    override fun hasLocalFailed(local: RepositoryGroup): Boolean {
        return local is Failure
    }

    override suspend fun updateLocal(newRemote: ServiceGroup, oldLocal: RepositoryGroup) {
        when (newRemote) {
            is Failure -> onRemoteFailure(newRemote.error, oldLocal)
            is Success -> localGroupRepository.upsert(newRemote.value)
        }
    }

    private fun onRemoteFailure(
        remoteError: GetGroupError,
        oldLocal: RepositoryGroup,
    ) {
        if (remoteError == GetGroupError.NotFound && oldLocal is Success) {
            TODO("Delete ${oldLocal.value}")
        }
    }
}

private typealias RepositoryGroup = Outcome<LocalGroup, GetError>
private typealias ServiceGroup = Outcome<LocalGroup, GetGroupError>