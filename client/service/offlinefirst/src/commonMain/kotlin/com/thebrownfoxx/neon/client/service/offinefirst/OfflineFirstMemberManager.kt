package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.client.repository.MemberRepository
import com.thebrownfoxx.neon.client.service.MemberManager
import com.thebrownfoxx.neon.client.service.MemberManager.GetMemberError
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.extension.mirrorTo
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class OfflineFirstMemberManager(
    private val remoteMemberManager: MemberManager,
    private val localMemberRepository: MemberRepository,
    externalScope: CoroutineScope,
) : MemberManager {
    private val memberCache = Cache<MemberId, Outcome<LocalMember, GetMemberError>>(externalScope)

    override fun getMember(id: MemberId): Flow<Outcome<LocalMember, GetMemberError>> {
        return memberCache.getAsFlow(id) {
            offlineFirst(
                localFlow = localMemberRepository.getAsFlow(id),
                remoteFlow = remoteMemberManager.getMember(id),
            ) { localMemberRepository.upsert(it) }.mirrorTo(this)
        }
    }
}