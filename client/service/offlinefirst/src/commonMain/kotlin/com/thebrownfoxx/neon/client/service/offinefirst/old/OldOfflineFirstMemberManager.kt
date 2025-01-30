package com.thebrownfoxx.neon.client.service.offinefirst.old

import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.client.repository.LocalMemberRepository
import com.thebrownfoxx.neon.client.service.MemberManager
import com.thebrownfoxx.neon.client.service.MemberManager.GetMemberError
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.extension.failedWith
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.mapError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Deprecated("Use OfflineFFirstMemberManager instead")
class OldOfflineFirstMemberManager(
    private val remoteMemberManager: MemberManager,
    private val localMemberRepository: LocalMemberRepository,
    externalScope: CoroutineScope,
) : MemberManager {
    private val getMemberCache =
        Cache<MemberId, Outcome<LocalMember, GetMemberError>>(externalScope)

    override fun getMember(id: MemberId): Flow<Outcome<LocalMember, GetMemberError>> {
        return getMemberCache.getOrInitialize(id) {
            val mappedLocalFlow = localMemberRepository.getAsFlow(id).map { local ->
                local.mapError { it.toGetMemberError() }
            }

            offlineFirst(
                localFlow = mappedLocalFlow,
                remoteFlow = remoteMemberManager.getMember(id),
            ) {
                defaultTransform(
                    succeeded = { it is Success },
                    notFound = { it.failedWith(GetMemberError.NotFound) },
                    failedUnexpectedly = { it.failedWith(GetMemberError.UnexpectedError) },
                    updateLocal = { updateMember(it) },
                    deleteLocal = { TODO() },
                )
            }
        }
    }

    private fun GetError.toGetMemberError() = when (this) {
        GetError.NotFound -> GetMemberError.NotFound
        GetError.ConnectionError, GetError.UnexpectedError -> GetMemberError.UnexpectedError
    }

    private suspend fun updateMember(memberOutcome: Outcome<LocalMember, *>) {
        val member = memberOutcome.getOrElse { return }
        localMemberRepository.upsert(member)
    }
}