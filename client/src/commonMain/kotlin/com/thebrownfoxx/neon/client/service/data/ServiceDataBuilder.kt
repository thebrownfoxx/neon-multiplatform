package com.thebrownfoxx.neon.client.service.data

import com.thebrownfoxx.neon.common.model.ChatGroup
import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.type.Url

fun serviceData(builder: ServiceDataBuilder): ServiceData {
    val builderScope = with(builder) {
        ServiceDataBuilderScope().apply { invoke() }
    }

    return builderScope.build()
}

fun interface ServiceDataBuilder {
    fun ServiceDataBuilderScope.invoke()
}

class ServiceDataBuilderScope internal constructor() {
    private val members = mutableListOf<MemberData>()
    private val groups = mutableListOf<GroupData>()
    private val conversations = mutableListOf<Conversation>()

    fun member(username: String, avatar: Url?, password: String): MemberId {
        val member = Member(username = username, avatarUrl = avatar)
        members.add(MemberData(member, password))
        return member.id
    }

    fun community(
        name: String,
        avatarUrl: Url?,
        members: List<MemberId>,
    ): GroupId {
        val group = Community(name = name, avatarUrl = avatarUrl)
        groups.add(GroupData(group, members))
        return group.id
    }

    fun conversation(vararg members: MemberId, builder: ConversationBuilder) {
        val group = ChatGroup()
        groups.add(GroupData(group, members.toList()))
        group.id.conversation(builder)
    }

    fun GroupId.conversation(builder: ConversationBuilder) {
        val conversationBuilderScope = with(builder) {
            ConversationBuilderScope(this@conversation).apply { invoke() }
        }

        conversations.add(conversationBuilderScope.build())
    }

    internal fun build() = ServiceData(members, groups, conversations)
}

data class MemberData(
    val member: Member,
    val password: String,
)

data class GroupData(
    val group: Group,
    val members: List<MemberId>,
)