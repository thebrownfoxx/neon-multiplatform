package com.thebrownfoxx.neon.server.repository.data

import com.thebrownfoxx.neon.common.data.transaction.transaction
import com.thebrownfoxx.neon.common.extension.onFailure
import com.thebrownfoxx.neon.common.extension.supervisorScope
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
import com.thebrownfoxx.outcome.map.flatMapError
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.onFailure
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

suspend fun ServiceData.integrate(
    configurationRepository: ConfigurationRepository,
    groupRepository: GroupRepository,
    memberRepository: MemberRepository,
    groupMemberRepository: GroupMemberRepository,
    inviteCodeRepository: InviteCodeRepository,
    passwordRepository: PasswordRepository,
    messageRepository: MessageRepository,
    hasher: Hasher,
): UnitOutcome<IntegrationError> {
    if (configurationRepository.getInitialized().getOrElse { return Failure(IntegrationError) })
        return UnitSuccess

    logInfo("Integrating $this")
    return supervisorScope {
        transaction {
            groupRecords.map { groupRecord ->
                async groupRecords@{
                    val (group, memberIds) = groupRecord

                    groupRepository.add(groupRecord.group).register()
                        .onFailure { return@groupRecords Failure(IntegrationError) }

                    memberIds.map { memberId ->
                        async {
                            groupMemberRepository.addMember(group.id, memberId).register()
                                .onFailure { return@async Failure(IntegrationError) }

                            UnitSuccess
                        }
                    }
                        .awaitAll()
                        .onFailure { return@groupRecords Failure(IntegrationError) }

                    if (groupRecord !is CommunityRecord) return@groupRecords UnitSuccess

                    val inviteCode = groupRecord.inviteCode ?: return@groupRecords UnitSuccess

                    inviteCodeRepository.set(group.id, inviteCode).register()
                        .onFailure { return@groupRecords Failure(IntegrationError) }

                    UnitSuccess
                }
            }
                .awaitAll()
                .onFailure { return@transaction Failure(IntegrationError) }

            memberRecords.map { (member, _, password) ->
                async {
                    memberRepository.add(member).register()
                        .onFailure { return@async Failure(IntegrationError) }
                    passwordRepository.setHash(member.id, hasher.hash(password)).register()
                        .onFailure { return@async Failure(IntegrationError) }

                    UnitSuccess
                }
            }
                .awaitAll()
                .onFailure { return@transaction Failure(IntegrationError) }

            messages.map { message ->
                async {
                    messageRepository.add(message).register()
                        .onFailure { return@async Failure(IntegrationError) }
                }
            }
                .awaitAll()
                .onFailure { return@transaction Failure(IntegrationError) }

            configurationRepository.setInitialized(true).register()
                .onFailure { return@transaction Failure(IntegrationError) }

            UnitSuccess
        }
    }.flatMapError { IntegrationError }
}

data object IntegrationError