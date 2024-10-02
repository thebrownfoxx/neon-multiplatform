package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import kotlinx.coroutines.flow.Flow

interface GroupManager {
    fun getGroup(groupId: GroupId): Flow<Group>
    suspend fun createGroup(group: Group)
    suspend fun addMember(groupId: GroupId, memberId: MemberId)
}