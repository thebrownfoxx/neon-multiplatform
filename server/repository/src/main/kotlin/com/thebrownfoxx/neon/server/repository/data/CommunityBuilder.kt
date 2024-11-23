package com.thebrownfoxx.neon.server.repository.data

import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.repository.data.model.CommunityRecord
import com.thebrownfoxx.neon.server.repository.data.model.GroupRecord
import com.thebrownfoxx.neon.server.repository.data.model.MemberRecord

typealias CommunityBuilder = CommunityBuilderScope.() -> Unit

open class CommunityBuilderScope internal constructor(
    protected val name: String,
    protected val avatarUrl: Url?,
    protected val god: Boolean,
) {
    private val memberIds = mutableSetOf<MemberId>()

    fun member(id: MemberId): MemberId {
        memberIds.add(id)
        return id
    }

    internal open fun build(): CommunityBuilderData {
        val community = Community(
            name = name,
            avatarUrl = avatarUrl,
            god = god,
        )

        val communityRecord =
            CommunityRecord(
                group = community,
                memberIds = memberIds,
                inviteCode = null,
            )

        return CommunityBuilderData(communityRecord, emptyList())
    }
}

internal data class CommunityBuilderData(
    val communityRecord: GroupRecord,
    val memberRecords: List<MemberRecord>,
)
