package com.thebrownfoxx.neon.server.repository.data

import com.thebrownfoxx.neon.common.data.transaction.transaction
import com.thebrownfoxx.neon.common.hash.Hasher
import com.thebrownfoxx.neon.common.logInfo
import com.thebrownfoxx.neon.server.repository.ConfigurationRepository
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.GroupRepository
import com.thebrownfoxx.neon.server.repository.InviteCodeRepository
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.MessageRepository
import com.thebrownfoxx.neon.server.repository.PasswordRepository
import com.thebrownfoxx.neon.server.repository.data.model.CommunityRecord
import com.thebrownfoxx.neon.server.repository.data.model.ServiceData
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.onFailure

suspend fun ServiceData.integrate(
    configurationRepository: ConfigurationRepository,
    groupRepository: GroupRepository,
    memberRepository: MemberRepository,
    groupMemberRepository: GroupMemberRepository,
    inviteCodeRepository: InviteCodeRepository,
    passwordRepository: PasswordRepository,
    messageRepository: MessageRepository,
    hasher: Hasher,
): UnitOutcome<Any> {
    if (configurationRepository.getInitialized().getOrElse { return Failure(it) })
        return UnitSuccess

    logInfo("Integrating $this")
    return transaction {
        for (groupRecord in groupRecords) {
            val (group, memberIds) = groupRecord

            groupRepository.add(groupRecord.group).register()
                .onFailure { return@transaction Failure(it) }

            for (memberId in memberIds) {
                groupMemberRepository.addMember(group.id, memberId).register()
                    .onFailure { return@transaction Failure(it) }
            }

            if (groupRecord is CommunityRecord) {
                val inviteCode = groupRecord.inviteCode

                if (inviteCode != null) {
                    inviteCodeRepository.set(group.id, inviteCode).register()
                        .onFailure { return@transaction Failure(it) }
                }
            }
        }

        for ((member, _, password) in memberRecords) {
            memberRepository.add(member).register()
                .onFailure { return@transaction Failure(it) }
            passwordRepository.setHash(member.id, hasher.hash(password)).register()
                .onFailure { return@transaction Failure(it) }
        }

        for (message in messages) {
            messageRepository.add(message).register()
                .onFailure { return@transaction Failure(it) }
        }

        configurationRepository.setInitialized(true).register()
            .onFailure { return@transaction Failure(it) }

        UnitSuccess
    }
}