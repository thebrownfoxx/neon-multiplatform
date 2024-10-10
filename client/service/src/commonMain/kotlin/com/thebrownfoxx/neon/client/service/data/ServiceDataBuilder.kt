package com.thebrownfoxx.neon.client.service.data

import com.thebrownfoxx.neon.client.service.data.model.ChatGroupRecord
import com.thebrownfoxx.neon.client.service.data.model.GroupRecord
import com.thebrownfoxx.neon.client.service.data.model.MemberRecord
import com.thebrownfoxx.neon.client.service.data.model.ServiceData
import com.thebrownfoxx.neon.common.model.ChatGroup
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.type.Url

typealias ServiceDataBuilder = ServiceDataBuilderScope.() -> Unit

fun serviceData(builder: ServiceDataBuilder): ServiceData {
    val builderScope = ServiceDataBuilderScope().apply { builder() }
    return builderScope.build()
}

class ServiceDataBuilderScope internal constructor() {
    private val groups = mutableListOf<GroupRecord>()
    private val members = mutableListOf<MemberRecord>()
    private val messages = mutableListOf<Message>()

    fun community(
        name: String,
        avatarUrl: Url?,
        inviteCode: String,
        builder: CommunityBuilder = {},
    ): GroupId {
        val communityBuilderScope = CommunityBuilderScope(
            name = name,
            avatarUrl = avatarUrl,
            inviteCode = inviteCode,
        ).apply(builder)

        val communityBuilderData = communityBuilderScope.build()

        groups.add(communityBuilderData.communityRecord)
        members.addAll(communityBuilderData.memberRecords)

        return communityBuilderData.communityRecord.group.id
    }

    fun GroupId.conversation(builder: ConversationBuilder) {
        val conversationBuilderScope = ConversationBuilderScope(this).apply(builder)
        messages.addAll(conversationBuilderScope.build())
    }

    fun conversation(vararg members: MemberId, builder: ConversationBuilder) {
        val group = ChatGroup()
        groups.add(ChatGroupRecord(group, members.toSet()))
        group.id.conversation(builder)
    }

    internal fun build() = ServiceData(members, groups, messages)
}