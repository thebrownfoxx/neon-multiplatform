package com.thebrownfoxx.neon.client.service.offinefirst.member

import com.thebrownfoxx.neon.client.converter.toLocalMember
import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.client.remote.RemoteMemberManager.GetMemberError
import com.thebrownfoxx.neon.client.repository.LocalMemberRepository
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstHandler
import com.thebrownfoxx.neon.client.service.offinefirst.offlineFirstCacheMap
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import kotlinx.coroutines.CoroutineScope

internal fun createMemberCache(externalScope: CoroutineScope) =
    offlineFirstCacheMap<MemberId, RepositoryMember, RemoteMember>(externalScope)

internal class MemberOfflineFirstHandler(
    private val localMemberRepository: LocalMemberRepository,
) : OfflineFirstHandler<RepositoryMember, RemoteMember> {
    override fun hasLocalFailed(local: RepositoryMember): Boolean {
        return local is Failure
    }

    override suspend fun updateLocal(newRemote: RemoteMember, oldLocal: RepositoryMember) {
        when (newRemote) {
            is Failure -> onRemoteFailure(newRemote.error, oldLocal)
            is Success -> localMemberRepository.upsert(newRemote.value.toLocalMember())
        }
    }

    private fun onRemoteFailure(
        remoteError: GetMemberError,
        oldLocal: RepositoryMember,
    ) {
        if (remoteError == GetMemberError.NotFound && oldLocal is Success) {
            TODO("Delete ${oldLocal.value}")
        }
    }
}

private typealias RepositoryMember = Outcome<LocalMember, GetError>
private typealias RemoteMember = Outcome<Member, GetMemberError>