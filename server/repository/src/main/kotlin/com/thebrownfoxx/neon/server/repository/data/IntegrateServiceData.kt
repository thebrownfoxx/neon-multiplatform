package com.thebrownfoxx.neon.server.repository.data

import com.thebrownfoxx.neon.common.hash.Hasher
import com.thebrownfoxx.neon.server.repository.data.model.ChatGroupRecord
import com.thebrownfoxx.neon.server.repository.data.model.CommunityRecord
import com.thebrownfoxx.neon.server.repository.data.model.ServiceData
import com.thebrownfoxx.neon.server.repository.group.GroupRepository
import com.thebrownfoxx.neon.server.repository.groupmember.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.invite.InviteCodeRepository
import com.thebrownfoxx.neon.server.repository.member.MemberRepository
import com.thebrownfoxx.neon.server.repository.password.PasswordRepository

suspend fun ServiceData.integrate(
    groupRepository: GroupRepository,
    memberRepository: MemberRepository,
    groupMemberRepository: GroupMemberRepository,
    inviteCodeRepository: InviteCodeRepository,
    passwordRepository: PasswordRepository,
    hasher: Hasher,
) {
    val communityRecords = groupRecords.filterIsInstance<CommunityRecord>()

    for (communityRecord in communityRecords) {
        groupManager.createCommunity(
            name = communityRecord.group.name,
        )
    }

    for (memberRecords in memberRecords) {
        memberManager.registerMember(
            inviteCode = memberRecords.inviteCode,
            username = memberRecords.member.username,
            password = memberRecords.password,
        )
    }

    val chatGroupRecords = groupRecords.filterIsInstance<ChatGroupRecord>()

    for (chatGroupRecord in chatGroupRecords) {
        messenger.newConversation(chatGroupRecord.memberIds)
    }

    for (message in messages) {
        messenger.sendMessage(message.groupId, message.content)
    }
}