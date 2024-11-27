package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.unitSuccess
import com.thebrownfoxx.neon.server.repository.InviteCodeRepository
import com.thebrownfoxx.neon.server.repository.RepositorySetInviteCodeError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

private typealias InviteCode = String

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryInviteCodeRepository : InviteCodeRepository {
    private val inviteCodes = MutableStateFlow<Map<GroupId, InviteCode>>(emptyMap())

    override fun get(groupId: GroupId): Flow<Outcome<String, GetError>> {
        return inviteCodes.mapLatest { inviteCodes ->
            when (val inviteCode = inviteCodes[groupId]) {
                null -> Failure(GetError.NotFound)
                else -> Success(inviteCode)
            }
        }
    }

    override suspend fun getGroup(
        inviteCode: String,
    ): Outcome<GroupId, GetError> {
        val group = inviteCodes.value
            .filter { (_, groupInviteCode) -> groupInviteCode == inviteCode }
            .map { it.key }
            .firstOrNull()

        return when (group) {
            null -> Failure(GetError.NotFound)
            else -> Success(group)
        }
    }

    override suspend fun set(
        groupId: GroupId,
        inviteCode: String,
    ): UnitOutcome<RepositorySetInviteCodeError> {
        if (inviteCode in inviteCodes.value.values)
            return Failure(RepositorySetInviteCodeError.DuplicateInviteCode)

        inviteCodes.update { it + (groupId to inviteCode) }
        return unitSuccess()
    }
}