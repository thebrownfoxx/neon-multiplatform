package com.thebrownfoxx.neon.server.repository.data

import com.thebrownfoxx.neon.common.hash.Hasher
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.common.type.onFailure
import com.thebrownfoxx.neon.server.repository.data.model.CommunityRecord
import com.thebrownfoxx.neon.server.repository.data.model.ServiceData
import com.thebrownfoxx.neon.server.repository.group.GroupRepository
import com.thebrownfoxx.neon.server.repository.groupmember.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.invite.InviteCodeRepository
import com.thebrownfoxx.neon.server.repository.member.MemberRepository
import com.thebrownfoxx.neon.server.repository.message.MessageRepository
import com.thebrownfoxx.neon.server.repository.password.PasswordRepository

suspend fun ServiceData.integrate(
    groupRepository: GroupRepository,
    memberRepository: MemberRepository,
    groupMemberRepository: GroupMemberRepository,
    inviteCodeRepository: InviteCodeRepository,
    passwordRepository: PasswordRepository,
    messageRepository: MessageRepository,
    hasher: Hasher,
) {
    for (groupRecord in groupRecords) {
        val (group, memberIds) = groupRecord

        groupRepository.add(groupRecord.group).onFailure { error() }

        for (memberId in memberIds) {
            groupMemberRepository.addMember(group.id, memberId).onFailure { error() }
        }

        if (groupRecord is CommunityRecord) {
            val inviteCode = groupRecord.inviteCode

            if (inviteCode != null) {
                inviteCodeRepository.set(group.id, inviteCode).onFailure { error() }
            }
        }
    }

    for ((member, inviteCode, password) in memberRecords) {
        memberRepository.add(member).onFailure { error() }
        val groupId = inviteCodeRepository.getGroup(inviteCode).getOrElse { error() }
        groupMemberRepository.addMember(groupId, member.id)
        passwordRepository.setHash(member.id, hasher.hash(password))
    }

    for (message in messages) {
        messageRepository.add(message)
    }
}

private fun ServiceData.error(): Nothing {
    error("Illegal service data $this")
}