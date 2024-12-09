package com.thebrownfoxx.neon.server.repository.data

import com.thebrownfoxx.neon.common.data.transaction.transaction
import com.thebrownfoxx.neon.common.hash.Hasher
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
import com.thebrownfoxx.outcome.getOrElse
import com.thebrownfoxx.outcome.mapError
import com.thebrownfoxx.outcome.onFailure

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
    if (configurationRepository.getInitialized().getOrElse { return mapError(error) })
        return UnitSuccess

    println("Integrating $this")
    return transaction {
        for (groupRecord in groupRecords) {
            val (group, memberIds) = groupRecord

            groupRepository.add(groupRecord.group).register()
                .onFailure { return@transaction mapError(error) }

            for (memberId in memberIds) {
                groupMemberRepository.addMember(group.id, memberId).register()
                    .onFailure { return@transaction mapError(error) }
            }

            if (groupRecord is CommunityRecord) {
                val inviteCode = groupRecord.inviteCode

                if (inviteCode != null) {
                    inviteCodeRepository.set(group.id, inviteCode).register()
                        .onFailure { return@transaction mapError(error) }
                }
            }
        }

        for ((member, _, password) in memberRecords) {
            memberRepository.add(member).register()
                .onFailure { return@transaction mapError(error) }
            passwordRepository.setHash(member.id, hasher.hash(password)).register()
                .onFailure { return@transaction mapError(error) }
        }

        for (message in messages) {
            messageRepository.add(message).register()
                .onFailure { return@transaction mapError(error) }
        }

        configurationRepository.setInitialized(true).register()
            .onFailure { return@transaction mapError(error) }

        UnitSuccess
    }
}