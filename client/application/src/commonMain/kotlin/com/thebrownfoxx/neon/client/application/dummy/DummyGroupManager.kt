package com.thebrownfoxx.neon.client.application.dummy

import com.thebrownfoxx.neon.client.model.LocalChatGroup
import com.thebrownfoxx.neon.client.model.LocalCommunity
import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.GroupManager.GetGroupError
import com.thebrownfoxx.neon.client.service.GroupManager.UnexpectedGetMembersError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DummyGroupManager(
    private val getGroupDelay: Duration = 0.seconds,
    private val getMembersDelay: Duration = 0.seconds,
) : GroupManager {
    private val generatedGroups = mutableMapOf<GroupId, LocalGroup>()
    private val generatedMembers = mutableMapOf<GroupId, Set<MemberId>>()

    override fun getGroup(id: GroupId): Flow<Outcome<LocalGroup, GetGroupError>> {
        return flow {
            emit(Success(generatedGroups.getOrPut(id) { generateGroup(id) }))
        }
    }

    override fun getMembers(
        groupId: GroupId,
    ): Flow<Outcome<Set<MemberId>, UnexpectedGetMembersError>> {
        return flow {
            emit(Success(generatedMembers.getOrPut(groupId) { generateMembers() }))
        }
    }

    private suspend fun generateGroup(id: GroupId): LocalGroup {
        delay(getGroupDelay)
        return when {
            Random.nextBoolean() -> generateChatGroup(id)
            else -> generateCommunity(id)
        }
    }

    private fun generateChatGroup(id: GroupId) = LocalChatGroup(id = id)

    private fun generateCommunity(id: GroupId) = LocalCommunity(
        id = id,
        name = "Community ${Random.nextInt()}",
        avatarUrl = null,
        isGod = false,
    )

    private suspend fun generateMembers(): Set<MemberId> {
        delay(getMembersDelay)
        return List((1..55).random()) { MemberId() }.toSet()
    }
}