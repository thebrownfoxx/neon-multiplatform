package com.thebrownfoxx.neon.client.service.offinefirst.member

import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.client.repository.MemberRepository
import com.thebrownfoxx.neon.client.service.MemberManager.GetMemberError
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstHandler
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success

class MemberOfflineFirstHandler(
    private val localMemberRepository: MemberRepository,
) : OfflineFirstHandler<RepositoryMember, ServiceMember> {
    override fun hasLocalFailed(local: RepositoryMember): Boolean {
        return local is Failure
    }

    override suspend fun updateLocal(newRemote: ServiceMember, oldLocal: RepositoryMember) {
        when (newRemote) {
            is Failure -> onRemoteFailure(newRemote.error, oldLocal)
            is Success -> localMemberRepository.upsert(newRemote.value)
        }
    }

    private fun onRemoteFailure(
        remoteError: GetMemberError,
        oldLocal: RepositoryMember,
    ) {
        if (remoteError != GetMemberError.NotFound || oldLocal !is Success) return
        TODO("Delete ${oldLocal.value}")
    }
}

private typealias RepositoryMember = Outcome<LocalMember, GetError>
private typealias ServiceMember = Outcome<LocalMember, GetMemberError>