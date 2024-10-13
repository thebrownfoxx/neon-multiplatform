package com.thebrownfoxx.neon.client.service.group

import com.thebrownfoxx.neon.client.service.group.model.AddGroupMemberError
import com.thebrownfoxx.neon.client.service.group.model.CreateCommunityError
import com.thebrownfoxx.neon.client.service.group.model.GetGroupError
import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import kotlinx.coroutines.flow.Flow

interface GroupManager {
    fun getGroup(id: GroupId): Flow<Result<Group, GetGroupError>>

    suspend fun createCommunity(
        name: String,
        inviteCode: String,
    ): Result<GroupId, CreateCommunityError>

    suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean = false,
    ): UnitResult<AddGroupMemberError>
}