package com.thebrownfoxx.neon.client.service.data

import com.thebrownfoxx.neon.client.service.data.model.MemberRecord
import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.common.model.Delivery
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.must.contentMustEqual
import kotlinx.datetime.Instant
import kotlin.test.Test

class ServiceDataBuilderTest {
    @Test
    fun memberShouldReturnCorrectId() {
        val usernames = listOf("lando_norris", "carlos_sainz")

        val actualMemberIds = mutableSetOf<MemberId>()

        val serviceData = serviceData {
            community(
                name = "neon",
                avatarUrl = Url("https://neon.thebrownfoxx.com/icon.png"),
                inviteCode = "neon",
            ) {
                for (username in usernames) {
                    val memberId = member(
                        username = username,
                        avatarUrl = null,
                        password = "",
                    )
                    actualMemberIds.add(memberId)
                }
            }
        }

        val expectedMemberIds = serviceData.memberRecords.map { it.member.id }

        actualMemberIds contentMustEqual expectedMemberIds
    }

    @Test
    fun shouldAddMembersProperly() {
        val inviteCode = "neon"

        val expectedMembers = listOf(
            MemberRecord(
                member = Member(
                    username = "lando_norris",
                    avatarUrl = Url("https://example.com/lando.jpg"),
                ).ignoreId(),
                inviteCode = inviteCode,
                password = "carlos sainz",
            ),
            MemberRecord(
                member = Member(
                    username = "carlos_sainz",
                    avatarUrl = Url("https://example.com/carlos.jpg"),
                ).ignoreId(),
                inviteCode = inviteCode,
                password = "lando norris",
            ),
        )

        val serviceData = serviceData {
            community(
                name = "neon",
                avatarUrl = Url("https://neon.thebrownfoxx.com/icon.png"),
                inviteCode = "neon",
            ) {
                for (expectedMember in expectedMembers) {
                    member(
                        username = expectedMember.member.username,
                        avatarUrl = expectedMember.member.avatarUrl,
                        password = expectedMember.password,
                    )
                }
            }
        }

        val actualMembers = serviceData.memberRecords.map { it.copy(member = it.member.ignoreId()) }

        actualMembers contentMustEqual expectedMembers
    }

    @Test
    fun communityShouldReturnCorrectId() {
        val communityNames = listOf("Formula 1", "Formula 2")

        val actualGroupIds = mutableSetOf<GroupId>()

        val serviceData = serviceData {
            for (communityName in communityNames) {
                val groupId = community(
                    name = communityName,
                    avatarUrl = Url("https://example.com/$communityName.jpg"),
                    inviteCode = communityName,
                )
                actualGroupIds.add(groupId)
            }
        }

        val expectedGroupIds = serviceData.groupRecords.map { it.group.id }

        actualGroupIds contentMustEqual expectedGroupIds
    }

    @Test
    fun shouldAddCommunitiesProperly() {
        val expectedCommunities = listOf(
            Community(
                name = "Formula 1",
                avatarUrl = Url("https://example.com/formula1.jpg"),
                inviteCode = "f1",
            ).ignoreId(),
            Community(
                name = "Formula 2",
                avatarUrl = Url("https://example.com/formula2.jpg"),
                inviteCode = "f2",
            ).ignoreId(),
        )

        val serviceData = serviceData {
            for (expectedCommunity in expectedCommunities) {
                community(
                    name = expectedCommunity.name,
                    avatarUrl = expectedCommunity.avatarUrl,
                    inviteCode = expectedCommunity.inviteCode,
                )
            }
        }

        val actualCommunities = serviceData.groupRecords.map { it.group.ignoreId() }

        actualCommunities contentMustEqual expectedCommunities
    }

    @Test
    fun shouldAddCommunityMembersProperly() {
        val community = Community(
            name = "Formula 1",
            inviteCode = "f1",
            avatarUrl = Url("https://example.com/formula1.jpg"),
        )

        val expectedMemberIds = mutableSetOf<MemberId>()

        val serviceData = serviceData {
            community(
                name = community.name,
                avatarUrl = community.avatarUrl,
                inviteCode = community.inviteCode,
            ) {
                for (index in 0..2) {
                    val memberId = member(
                        username = "member$index",
                        avatarUrl = null,
                        password = "password$index",
                    )
                    expectedMemberIds.add(memberId)
                }
            }
        }

        val actualMemberIds = serviceData.groupRecords.single().memberIds

        actualMemberIds contentMustEqual expectedMemberIds
    }

    @Test
    fun shouldAddMessagesToCommunityProperly() {
        val groupId = GroupId()
        val memberIds = listOf(MemberId(), MemberId(), MemberId())

        val expectedMessages = listOf(
            Message(
                groupId = groupId,
                senderId = memberIds[0],
                content = "Hello",
                timestamp = Instant.fromEpochMilliseconds(0),
                delivery = Delivery.Read,
            ).ignoreId(),
            Message(
                groupId = groupId,
                senderId = memberIds[1],
                content = "World",
                timestamp = Instant.fromEpochMilliseconds(1),
                delivery = Delivery.Delivered,
            ).ignoreId(),
            Message(
                groupId = groupId,
                senderId = memberIds[2],
                content = "!",
                timestamp = Instant.fromEpochMilliseconds(2),
                delivery = Delivery.Sent,
            ).ignoreId(),
        )

        val serviceData = serviceData {
            groupId.conversation {
                for (message in expectedMessages) {
                    message.senderId.said(
                        content = message.content,
                        timestamp = message.timestamp,
                        delivery = message.delivery,
                    )
                }
            }
        }

        val actualMessages = serviceData.messages.map { it.ignoreId() }

        actualMessages contentMustEqual expectedMessages
    }

    @Test
    fun grouplessConversationShouldCreateAGroup() {
        val expectedMemberIds = setOf(MemberId(), MemberId())

        val serviceData = serviceData { conversation(*expectedMemberIds.toTypedArray()) {} }

        val actualMemberIds = serviceData.groupRecords.single().memberIds

        actualMemberIds contentMustEqual expectedMemberIds
    }

    @Test
    fun grouplessConversationShouldAddMessagesProperly() {
        data class GrouplessMessage(
            val senderId: MemberId,
            val content: String,
            val timestamp: Instant,
            val delivery: Delivery,
        )

        val memberIds = listOf(MemberId(), MemberId(), MemberId())

        val expectedMessages = listOf(
            GrouplessMessage(
                senderId = memberIds[0],
                content = "Hello",
                timestamp = Instant.fromEpochMilliseconds(0),
                delivery = Delivery.Read,
            ),
            GrouplessMessage(
                senderId = memberIds[1],
                content = "World",
                timestamp = Instant.fromEpochMilliseconds(1),
                delivery = Delivery.Delivered,
            ),
            GrouplessMessage(
                senderId = memberIds[2],
                content = "!",
                timestamp = Instant.fromEpochMilliseconds(2),
                delivery = Delivery.Sent,
            ),
        )

        val serviceData = serviceData {
            conversation(*memberIds.toTypedArray()) {
                for (message in expectedMessages) {
                    message.senderId.said(
                        content = message.content,
                        timestamp = message.timestamp,
                        delivery = message.delivery,
                    )
                }
            }
        }

        val actualMessages = serviceData.messages.map {
            GrouplessMessage(
                senderId = it.senderId,
                content = it.content,
                timestamp = it.timestamp,
                delivery = it.delivery,
            )
        }

        actualMessages contentMustEqual expectedMessages
    }
}