package com.thebrownfoxx.neon.server.service.data

import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.server.service.data.model.CommunityRecord
import com.thebrownfoxx.neon.server.service.data.model.GroupRecord
import com.thebrownfoxx.neon.server.service.data.model.MemberRecord

typealias CommunityBuilder = CommunityBuilderScope.() -> Unit

class CommunityBuilderScope internal constructor(
    private val name: String,
    private val avatarUrl: Url?,
    private val inviteCode: String,
    private val god: Boolean,
) {
    private val members = mutableListOf<MemberRecord>()
    private val memberIds = mutableListOf<MemberId>()

    fun member(
        username: String,
        avatarUrl: Url?,
        password: String,
    ): MemberId {
        val member = Member(username = username, avatarUrl = avatarUrl)
        members.add(MemberRecord(member = member, inviteCode = inviteCode, password = password))
        return member.id
    }

    fun member(id: MemberId): MemberId {
        memberIds.add(id)
        return id
    }


    internal fun build(): CommunityBuilderData {
        val community = Community(
            name = name,
            avatarUrl = avatarUrl,
            god = god,
        )
        val memberIds = (members.map { it.member.id } + memberIds).toSet()

        val communityRecord =
            CommunityRecord(
                group = community,
                memberIds = memberIds,
                inviteCode = inviteCode,
            )

        return CommunityBuilderData(communityRecord, members)
    }
}

internal data class CommunityBuilderData(
    val communityRecord: GroupRecord,
    val memberRecords: List<MemberRecord>,
)
