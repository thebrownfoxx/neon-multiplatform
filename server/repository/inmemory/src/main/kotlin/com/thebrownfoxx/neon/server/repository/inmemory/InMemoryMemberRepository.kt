package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.MemberRepository.AddMemberError
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.memberBlockContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryMemberRepository : MemberRepository {
    private val members = MutableStateFlow<Map<MemberId, Member>>(emptyMap())

    override fun getAsFlow(id: MemberId): Flow<Outcome<Member, GetError>> {
        memberBlockContext("getAsFlow") {
            return members.mapLatest { members ->
                when (val member = members[id]) {
                    null -> Failure(GetError.NotFound)
                    else -> Success(member)
                }
            }
        }
    }

    override suspend fun get(id: MemberId): Outcome<Member, GetError> {
        return getAsFlow(id).first()
    }

    override suspend fun getId(username: String): Outcome<MemberId, GetError> {
        memberBlockContext("getId") {
            return members.mapLatest { members ->
                when (val member = members.values.find { it.username == username }) {
                    null -> Failure(GetError.NotFound)
                    else -> Success(member.id)
                }
            }.first()
        }
    }

    override suspend fun add(member: Member): ReversibleUnitOutcome<AddMemberError> {
        memberBlockContext("add") {
            return when {
                members.value.containsKey(member.id) -> Failure(AddMemberError.DuplicateId)
                members.value.values.any { it.username == member.username } ->
                    Failure(AddMemberError.DuplicateUsername)

                else -> {
                    members.update { it + (member.id to member) }
                    UnitSuccess
                }
            }.asReversible { members.update { it - member.id } }
        }
    }
}