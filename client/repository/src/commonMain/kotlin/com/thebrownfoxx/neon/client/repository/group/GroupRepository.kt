package com.thebrownfoxx.neon.client.repository.group

import com.thebrownfoxx.neon.client.repository.group.model.AddGroupError
import com.thebrownfoxx.neon.client.repository.group.model.AddGroupMemberError
import com.thebrownfoxx.neon.client.repository.group.model.GetGroupError
import com.thebrownfoxx.neon.client.repository.group.model.GetGroupMembersError
import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun get(id: GroupId): Flow<Result<Group, GetGroupError>>
    fun getMembers(id: GroupId): Flow<Result<Set<MemberId>, GetGroupMembersError>>
    suspend fun add(group: Group): UnitResult<AddGroupError>
    suspend fun addMember(groupId: GroupId, memberId: MemberId): UnitResult<AddGroupMemberError>
}