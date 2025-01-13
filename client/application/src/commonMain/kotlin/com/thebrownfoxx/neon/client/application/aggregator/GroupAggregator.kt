package com.thebrownfoxx.neon.client.application.aggregator

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.state.toAvatarState
import com.thebrownfoxx.neon.client.model.LocalCommunity
import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.MemberManager
import com.thebrownfoxx.neon.common.extension.combineOrEmpty
import com.thebrownfoxx.neon.common.extension.flow
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.ThrowingApi
import com.thebrownfoxx.outcome.map.getOrThrow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class GroupAggregator(
    private val groupManager: GroupManager,
    private val memberManager: MemberManager,
) {
    @ThrowingApi
    fun getMembers(
        groupId: GroupId,
        count: Int = Int.MAX_VALUE,
        excludedIds: List<MemberId>,
    ): Flow<List<LocalMember>> {
        return groupManager.getMembers(groupId).flatMapLatest { members ->
            members.getOrThrow()
                .filter { it !in excludedIds }
                .take(count)
                .map { memberId ->
                    memberManager.getMember(memberId).mapLatest { it.getOrThrow() }
                }
                .combineOrEmpty { it.toList() }
        }
    }

    @ThrowingApi
    fun getCommunityAvatar(
        loggedInMemberId: MemberId?,
        community: LocalCommunity,
    ): Flow<AvatarState?> {
        if (community.avatarUrl != null) return SingleAvatarState(
            url = community.avatarUrl,
            placeholder = community.name,
        ).flow()
        return getMembers(
            groupId = community.id,
            count = 2,
            excludedIds = listOfNotNull(loggedInMemberId),
        ).mapLatest { it.toAvatarState() }
    }
}