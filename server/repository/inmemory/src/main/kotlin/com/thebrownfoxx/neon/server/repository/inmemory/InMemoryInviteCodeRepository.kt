package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.server.repository.InviteCodeRepository
import com.thebrownfoxx.neon.server.repository.InviteCodeRepository.SetInviteCodeError
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.memberBlockContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

private typealias InviteCode = String

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryInviteCodeRepository : InviteCodeRepository {
    private val inviteCodes = MutableStateFlow<Map<GroupId, InviteCode>>(emptyMap())

    override fun getAsFlow(groupId: GroupId): Flow<Outcome<String, GetError>> {
        memberBlockContext("getAsFlow") {
            return inviteCodes.mapLatest { inviteCodes ->
                when (val inviteCode = inviteCodes[groupId]) {
                    null -> Failure(GetError.NotFound)
                    else -> Success(inviteCode)
                }
            }
        }
    }

    override suspend fun getGroup(
        inviteCode: String,
    ): Outcome<GroupId, GetError> {
        memberBlockContext("getGroup") {
            val group = inviteCodes.value
                .filter { (_, groupInviteCode) -> groupInviteCode == inviteCode }
                .map { it.key }
                .firstOrNull()

            return when (group) {
                null -> Failure(GetError.NotFound)
                else -> Success(group)
            }
        }
    }

    override suspend fun set(
        groupId: GroupId,
        inviteCode: String,
    ): ReversibleUnitOutcome<SetInviteCodeError> {
        memberBlockContext("set") {
            if (inviteCode in inviteCodes.value.values)
                return Failure(SetInviteCodeError.DuplicateInviteCode).asReversible()

            inviteCodes.update { it + (groupId to inviteCode) }
            return UnitSuccess.asReversible { inviteCodes.update { it - groupId } }
        }
    }
}