package com.thebrownfoxx.neon.server.repository.data

import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.neon.server.repository.data.model.CommunityRecord
import com.thebrownfoxx.neon.server.repository.data.model.MemberRecord

typealias OpenCommunityBuilder = OpenCommunityBuilderScope.() -> Unit

class OpenCommunityBuilderScope internal constructor(
    name: String,
    avatarUrl: Url?,
    isGod: Boolean,
    private val inviteCode: String,
) : CommunityBuilderScope(name, avatarUrl, isGod) {
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


    override fun build(): CommunityBuilderData {
        val community = Community(
            name = name,
            avatarUrl = avatarUrl,
            isGod = isGod,
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