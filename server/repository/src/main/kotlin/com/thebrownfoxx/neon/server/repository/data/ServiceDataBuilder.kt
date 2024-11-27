package com.thebrownfoxx.neon.server.repository.data

import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.repository.data.model.GroupRecord
import com.thebrownfoxx.neon.server.repository.data.model.MemberRecord
import com.thebrownfoxx.neon.server.repository.data.model.ServiceData

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
        isGod: Boolean = false,
        builder: CommunityBuilder = {},
    ): GroupId {
        val communityBuilderScope = CommunityBuilderScope(
            name = name,
            avatarUrl = avatarUrl,
            isGod = isGod,
        ).apply(builder)
        return communityBuilderScope.build().apply()
    }

    fun community(
        name: String,
        avatarUrl: Url?,
        inviteCode: String,
        isGod: Boolean = false,
        builder: OpenCommunityBuilder = {},
    ): GroupId {
        val communityBuilderScope = OpenCommunityBuilderScope(
            name = name,
            avatarUrl = avatarUrl,
            inviteCode = inviteCode,
            isGod = isGod,
        ).apply(builder)
        return communityBuilderScope.build().apply()
    }

    private fun CommunityBuilderData.apply(): GroupId {
        groups.add(communityRecord)
        members.addAll(memberRecords)
        return communityRecord.group.id
    }

    fun conversation(builder: DirectConversationBuilder): GroupId {
        val directConversationRecord = DirectConversationBuilderScope().apply(builder).build()
        groups.add(directConversationRecord.chatGroupRecord)
        messages.addAll(directConversationRecord.messages)
        return directConversationRecord.chatGroupRecord.group.id
    }

    fun GroupId.conversation(builder: ConversationBuilder) {
        val conversationBuilderScope = ConversationBuilderScope(this).apply(builder)
        messages.addAll(conversationBuilderScope.build())
    }

    internal fun build() = ServiceData(members, groups, messages)
}