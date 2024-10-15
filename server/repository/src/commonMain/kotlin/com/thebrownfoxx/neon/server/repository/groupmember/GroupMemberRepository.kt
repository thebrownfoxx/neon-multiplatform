package com.thebrownfoxx.neon.server.repository.groupmember

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryAddGroupMemberError
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryGetAdminsError
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryGetGroupMembersError
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryGetMemberGroupsError
import kotlinx.coroutines.flow.Flow

interface GroupMemberRepository {
    fun getMembers(groupId: GroupId): Flow<Result<List<MemberId>, RepositoryGetGroupMembersError>>
    fun getGroups(memberId: MemberId): Flow<Result<List<GroupId>, RepositoryGetMemberGroupsError>>
    fun getAdmins(groupId: GroupId): Flow<Result<List<MemberId>, RepositoryGetAdminsError>>

    suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        admin: Boolean = false,
    ): UnitResult<RepositoryAddGroupMemberError>
}