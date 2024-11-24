package com.thebrownfoxx.neon.server.repository.invite

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.server.repository.invite.model.RepositoryGetInviteCodeError
import com.thebrownfoxx.neon.server.repository.invite.model.RepositoryGetInviteCodeGroupError
import com.thebrownfoxx.neon.server.repository.invite.model.RepositorySetInviteCodeError
import kotlinx.coroutines.flow.Flow

interface InviteCodeRepository {
    fun get(groupId: GroupId): Flow<Outcome<String, RepositoryGetInviteCodeError>>
    suspend fun getGroup(inviteCode: String): Outcome<GroupId, RepositoryGetInviteCodeGroupError>
    suspend fun set(groupId: GroupId, inviteCode: String): UnitOutcome<RepositorySetInviteCodeError>
}