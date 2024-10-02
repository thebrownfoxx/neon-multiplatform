package com.thebrownfoxx.neon.client.service.data

import com.thebrownfoxx.neon.client.service.data.model.MemberRecord
import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.common.model.Delivery
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.type.Url
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ServiceDataBuilderTest {
    @Test
    fun memberShouldReturnCorrectId() {
        val usernames = listOf("lando_norris", "carlos_sainz")

        val actualMemberIds = mutableListOf<MemberId>()

        val serviceData = serviceData {
            for (username in usernames) {
                val memberId = member(
                    username = username,
                    avatarUrl = null,
                    password = "",
                )
                actualMemberIds.add(memberId)
            }
        }

        val expectedMemberIds = serviceData.memberRecords.map { it.member.id }

        assertContentEquals(expectedMemberIds, actualMemberIds)
    }

    @Test
    fun shouldAddMembersProperly() {
        val expectedMembers = listOf(
            MemberRecord(
                member = Member(
                    username = "lando_norris",
                    avatarUrl = Url("https://example.com/lando.jpg"),
                ).ignoreId(),
                password = "carlos sainz",
            ),
            MemberRecord(
                member = Member(
                    username = "carlos_sainz",
                    avatarUrl = Url("https://example.com/carlos.jpg"),
                ).ignoreId(),
                password = "lando norris",
            ),
        )

        val serviceData = serviceData {
            for (expectedMember in expectedMembers) {
                member(
                    username = expectedMember.member.username,
                    avatarUrl = expectedMember.member.avatarUrl,
                    password = expectedMember.password,
                )
            }
        }

        val actualMembers = serviceData.memberRecords.map { it.copy(member = it.member.ignoreId()) }

        assertContentEquals(expectedMembers, actualMembers)
    }

    @Test
    fun communityShouldReturnCorrectId() {
        val communityNames = listOf("Formula 1", "Formula 2")

        val actualGroupIds = mutableListOf<GroupId>()

        val serviceData = serviceData {
            for (communityName in communityNames) {
                val groupId = community(
                    name = communityName,
                    avatarUrl = Url("https://example.com/$communityName.jpg"),
                    members = emptyList(),
                )
                actualGroupIds.add(groupId)
            }
        }

        val expectedGroupIds = serviceData.groupRecords.map { it.group.id }

        assertContentEquals(expectedGroupIds, actualGroupIds)
    }

    @Test
    fun shouldAddCommunitiesProperly() {
        val expectedCommunities = listOf(
            Community(
                name = "Formula 1",
                avatarUrl = Url("https://example.com/formula1.jpg"),
            ).ignoreId(),
            Community(
                name = "Formula 2",
                avatarUrl = Url("https://example.com/formula2.jpg"),
            ).ignoreId(),
        )

        val serviceData = serviceData {
            for (expectedCommunity in expectedCommunities) {
                community(
                    name = expectedCommunity.name,
                    avatarUrl = expectedCommunity.avatarUrl,
                    members = emptyList(),
                )
            }
        }

        val actualCommunities = serviceData.groupRecords.map { it.group.ignoreId() }

        assertContentEquals(expectedCommunities, actualCommunities)
    }

    @Test
    fun shouldAddCommunityMembersProperly() {
        val community = Community(
            name = "Formula 1",
            avatarUrl = Url("https://example.com/formula1.jpg"),
        )

        val expectedMemberIds = listOf(MemberId(), MemberId(), MemberId())

        val serviceData = serviceData {
            community(
                name = community.name,
                avatarUrl = community.avatarUrl,
                members = expectedMemberIds,
            )
        }

        val actualMemberIds = serviceData.groupRecords.single().memberIds

        assertContentEquals(expectedMemberIds, actualMemberIds)
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

        assertContentEquals(expectedMessages, actualMessages)
    }

    @Test
    fun grouplessConversationShouldCreateAGroup() {
        val expectedMemberIds = listOf(MemberId(), MemberId())

        val serviceData = serviceData { conversation(*expectedMemberIds.toTypedArray()) {} }

        val actualMemberIds = serviceData.groupRecords.single().memberIds

        assertEquals(expectedMemberIds, actualMemberIds)
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

        assertContentEquals(expectedMessages, actualMessages)
    }
}