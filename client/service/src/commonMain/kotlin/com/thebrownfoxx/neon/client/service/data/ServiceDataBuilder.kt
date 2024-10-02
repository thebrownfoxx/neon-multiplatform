package com.thebrownfoxx.neon.client.service.data

import com.thebrownfoxx.neon.client.service.data.model.GroupRecord
import com.thebrownfoxx.neon.client.service.data.model.MemberRecord
import com.thebrownfoxx.neon.client.service.data.model.ServiceData
import com.thebrownfoxx.neon.common.model.ChatGroup
import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.type.Url

typealias ServiceDataBuilder = ServiceDataBuilderScope.() -> Unit

fun serviceData(builder: ServiceDataBuilder): ServiceData {
    val builderScope = ServiceDataBuilderScope().apply { builder() }
    return builderScope.build()
}

class ServiceDataBuilderScope internal constructor() {
    private val members = mutableListOf<MemberRecord>()
    private val groups = mutableListOf<GroupRecord>()
    private val messages = mutableListOf<Message>()

    fun member(
        username: String,
        avatarUrl: Url?,
        password: String,
    ): MemberId {
        val member = Member(username = username, avatarUrl = avatarUrl)
        members.add(MemberRecord(member, password))
        return member.id
    }

    fun community(
        name: String,
        avatarUrl: Url?,
        members: List<MemberId>,
    ): GroupId {
        val group = Community(name = name, avatarUrl = avatarUrl)
        groups.add(GroupRecord(group, members))
        return group.id
    }

    fun GroupId.conversation(builder: ConversationBuilder) {
        val conversationBuilderScope = ConversationBuilderScope(this).apply { builder() }
        messages.addAll(conversationBuilderScope.build())
    }

    fun conversation(vararg members: MemberId, builder: ConversationBuilder) {
        val group = ChatGroup()
        groups.add(GroupRecord(group, members.toList()))
        group.id.conversation(builder)
    }

    internal fun build() = ServiceData(members, groups, messages)
}