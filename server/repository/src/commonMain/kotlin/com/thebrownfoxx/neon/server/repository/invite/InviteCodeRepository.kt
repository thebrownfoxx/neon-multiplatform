package com.thebrownfoxx.neon.server.repository.invite

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.repository.invite.model.RepositoryGetInviteCodeError
import com.thebrownfoxx.neon.server.repository.invite.model.RepositoryGetInviteCodeGroupError
import com.thebrownfoxx.neon.server.repository.invite.model.RepositorySetInviteCodeError

interface InviteCodeRepository {
    suspend fun get(groupId: GroupId): Result<String, RepositoryGetInviteCodeError>
    suspend fun getGroup(inviteCode: String): Result<GroupId, RepositoryGetInviteCodeGroupError>
    suspend fun set(groupId: GroupId, inviteCode: String): UnitResult<RepositorySetInviteCodeError>
}