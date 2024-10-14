package com.thebrownfoxx.neon.server.repository.group

import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.repository.group.model.AddGroupError
import com.thebrownfoxx.neon.server.repository.group.model.AddGroupMemberError
import com.thebrownfoxx.neon.server.repository.group.model.GetGroupError
import com.thebrownfoxx.neon.server.repository.group.model.GetGroupMembersError
import com.thebrownfoxx.neon.server.repository.group.model.GetInviteCodeGroupError
import com.thebrownfoxx.neon.server.repository.group.model.InGodCommunityError
import com.thebrownfoxx.neon.server.repository.group.model.IsGroupAdminError
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun get(id: GroupId): Flow<Result<Group, GetGroupError>>
    fun getInviteCodeGroup(inviteCode: String): Flow<Result<GroupId, GetInviteCodeGroupError>>
    fun getMembers(id: GroupId): Flow<Result<Set<MemberId>, GetGroupMembersError>>
    fun inGodCommunity(memberId: MemberId): Flow<Result<Boolean, InGodCommunityError>>
    fun isGroupAdmin(groupId: GroupId, memberId: MemberId): Flow<Result<Boolean, IsGroupAdminError>>

    suspend fun add(group: Group): UnitResult<AddGroupError>
    suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean = false,
    ): UnitResult<AddGroupMemberError>
}