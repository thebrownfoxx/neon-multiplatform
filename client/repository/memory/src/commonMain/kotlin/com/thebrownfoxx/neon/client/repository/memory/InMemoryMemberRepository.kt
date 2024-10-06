package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.MemberRepository
import com.thebrownfoxx.neon.client.repository.model.AddEntityError
import com.thebrownfoxx.neon.client.repository.model.AddEntityResult
import com.thebrownfoxx.neon.client.repository.model.GetEntityError
import com.thebrownfoxx.neon.client.repository.model.GetEntityResult
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InMemoryMemberRepository : MemberRepository {
    private val members = mutableMapOf<MemberId, Member>()

    override fun get(id: MemberId): Flow<GetEntityResult<Member>> {
        val result = when (val member = members[id]) {
            null -> Failure(GetEntityError.NotFound)
            else -> Success(member)
        }

        return flowOf(result)
    }

    override suspend fun add(member: Member): AddEntityResult {
        return when {
            members.containsKey(member.id) -> Failure(AddEntityError.DuplicateId)
            else -> {
                members[member.id] = member
                UnitSuccess()
            }
        }
    }
}