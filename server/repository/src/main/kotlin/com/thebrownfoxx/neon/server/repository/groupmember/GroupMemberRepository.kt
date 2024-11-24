package com.thebrownfoxx.neon.server.repository.groupmember

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryAddGroupMemberError
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryGetAdminsError
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryGetGroupMembersError
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryGetMemberGroupsError
import kotlinx.coroutines.flow.Flow

interface GroupMemberRepository {
    fun getMembers(groupId: GroupId): Flow<Outcome<List<MemberId>, RepositoryGetGroupMembersError>>
    fun getGroups(memberId: MemberId): Flow<Outcome<List<GroupId>, RepositoryGetMemberGroupsError>>
    fun getAdmins(groupId: GroupId): Flow<Outcome<List<MemberId>, RepositoryGetAdminsError>>

    suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        admin: Boolean = false,
    ): UnitOutcome<RepositoryAddGroupMemberError>
}