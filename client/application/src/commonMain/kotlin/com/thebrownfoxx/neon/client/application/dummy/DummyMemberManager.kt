package com.thebrownfoxx.neon.client.application.dummy

import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.client.service.MemberManager
import com.thebrownfoxx.neon.client.service.MemberManager.GetMemberError
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DummyMemberManager(
    private val delay: Duration = 0.seconds,
) : MemberManager {
    private val generatedMembers = mutableMapOf<MemberId, LocalMember>()

    override fun getMember(id: MemberId): Flow<Outcome<LocalMember, GetMemberError>> {
        return flow {
            emit(Success(generatedMembers.getOrPut(id) { generateMember(id) }))
        }
    }

    private suspend fun generateMember(id: MemberId): LocalMember {
        delay(delay)
        return LocalMember(
            id = id,
            username = "Member ${Random.nextInt()}",
            avatarUrl = null,
        )
    }
}