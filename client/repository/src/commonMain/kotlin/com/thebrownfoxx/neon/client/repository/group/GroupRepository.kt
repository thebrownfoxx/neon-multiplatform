package com.thebrownfoxx.neon.client.repository.group

import com.thebrownfoxx.neon.client.repository.group.model.AddGroupEntityError
import com.thebrownfoxx.neon.client.repository.group.model.AddGroupMemberEntityError
import com.thebrownfoxx.neon.client.repository.group.model.GetGroupEntityError
import com.thebrownfoxx.neon.client.repository.group.model.GetGroupMemberEntitiesError
import com.thebrownfoxx.neon.client.repository.group.model.GetInviteCodeGroupError
import com.thebrownfoxx.neon.client.repository.group.model.InGodCommunityError
import com.thebrownfoxx.neon.client.repository.group.model.IsGroupAdminError
import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun get(id: GroupId): Flow<Result<Group, GetGroupEntityError>>
    fun getInviteCodeGroup(inviteCode: String): Flow<Result<GroupId, GetInviteCodeGroupError>>
    fun getMembers(id: GroupId): Flow<Result<Set<MemberId>, GetGroupMemberEntitiesError>>
    fun inGodCommunity(memberId: MemberId): Flow<Result<Boolean, InGodCommunityError>>
    fun isGroupAdmin(groupId: GroupId, memberId: MemberId): Flow<Result<Boolean, IsGroupAdminError>>

    suspend fun add(group: Group): UnitResult<AddGroupEntityError>
    suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean = false,
    ): UnitResult<AddGroupMemberEntityError>
}