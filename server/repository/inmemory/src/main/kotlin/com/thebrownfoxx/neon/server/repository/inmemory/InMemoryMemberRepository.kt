package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.common.annotation.TestApi
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.common.model.unitSuccess
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.neon.server.repository.member.MemberRepository
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryAddMemberError
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberError
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberIdError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryMemberRepository : MemberRepository {
    private val members = MutableStateFlow<Map<MemberId, Member>>(emptyMap())

    @TestApi
    val memberList get() = members.value.map { it.value }

    override fun get(id: MemberId): Flow<Result<Member, RepositoryGetMemberError>> {
        return members.mapLatest { members ->
            when (val member = members[id]) {
                null -> Failure(RepositoryGetMemberError.NotFound)
                else -> Success(member)
            }
        }
    }

    override fun getId(username: String): Flow<Result<MemberId, RepositoryGetMemberIdError>> {
        return members.mapLatest { members ->
            when (val member = members.values.find { it.username == username }) {
                null -> Failure(RepositoryGetMemberIdError.NotFound)
                else -> Success(member.id)
            }
        }
    }

    override suspend fun add(member: Member): UnitResult<RepositoryAddMemberError> {
        return when {
            members.value.containsKey(member.id) -> Failure(RepositoryAddMemberError.DuplicateId)
            members.value.values.any { it.username == member.username } ->
                Failure(RepositoryAddMemberError.DuplicateUsername)

            else -> {
                members.update { it + (member.id to member) }
                unitSuccess()
            }
        }
    }
}