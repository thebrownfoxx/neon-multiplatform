package com.thebrownfoxx.neon.server.repository.groupmember

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.repository.groupmember.model.AddGroupMemberError
import com.thebrownfoxx.neon.server.repository.groupmember.model.GetAdminsError
import com.thebrownfoxx.neon.server.repository.groupmember.model.GetGroupMembersError
import com.thebrownfoxx.neon.server.repository.groupmember.model.GetMemberGroupsError
import kotlinx.coroutines.flow.Flow

interface GroupMemberRepository {
    fun getMembers(groupId: GroupId): Flow<Result<List<MemberId>, GetGroupMembersError>>
    fun getGroups(memberId: MemberId): Flow<Result<List<GroupId>, GetMemberGroupsError>>
    fun getAdmins(groupId: GroupId): Flow<Result<List<MemberId>, GetAdminsError>>

    suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        admin: Boolean = false,
    ): UnitResult<AddGroupMemberError>
}