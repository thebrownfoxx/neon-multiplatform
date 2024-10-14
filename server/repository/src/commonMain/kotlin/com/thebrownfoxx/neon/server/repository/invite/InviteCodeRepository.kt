package com.thebrownfoxx.neon.server.repository.invite

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.repository.invite.model.GetInviteCodeError
import com.thebrownfoxx.neon.server.repository.invite.model.GetInviteCodeGroupError
import com.thebrownfoxx.neon.server.repository.invite.model.SetInviteCodeError

interface InviteCodeRepository {
    suspend fun get(groupId: GroupId): Result<String, GetInviteCodeError>
    suspend fun getGroup(inviteCode: String): Result<GroupId, GetInviteCodeGroupError>
    suspend fun set(groupId: GroupId, inviteCode: String): UnitResult<SetInviteCodeError>
}