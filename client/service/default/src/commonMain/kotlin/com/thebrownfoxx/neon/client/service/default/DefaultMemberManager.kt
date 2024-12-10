package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.client.repository.MemberRepository
import com.thebrownfoxx.neon.client.service.MemberManager
import com.thebrownfoxx.neon.client.service.MemberManager.GetMemberError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.map.mapError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultMemberManager(private val memberRepository: MemberRepository) : MemberManager {
    override suspend fun getMember(
        id: MemberId,
    ): Flow<Outcome<LocalMember, GetMemberError>> {
        return memberRepository.get(id).map { outcome ->
            outcome.mapError { error ->
                when (error) {
                    GetError.NotFound -> GetMemberError.NotFound
                    GetError.ConnectionError, GetError.UnexpectedError ->
                        GetMemberError.UnexpectedError
                }
            }
        }
    }
}