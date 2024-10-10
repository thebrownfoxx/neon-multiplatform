package com.thebrownfoxx.neon.client.repository.test

import com.thebrownfoxx.neon.client.repository.group.GroupRepository
import com.thebrownfoxx.neon.client.repository.group.model.AddGroupEntityError
import com.thebrownfoxx.neon.client.repository.group.model.AddGroupMemberEntityError
import com.thebrownfoxx.neon.client.repository.group.model.GetGroupEntityError
import com.thebrownfoxx.neon.client.repository.group.model.GetGroupMemberEntitiesError
import com.thebrownfoxx.neon.common.model.ChatGroup
import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitSuccess
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.must.mustBe
import com.thebrownfoxx.neon.must.mustBeA
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

abstract class GroupRepositoryTest {
    // TODO: Test the flows for changes

    private val initialGroups = listOf(
        GroupWithMembers(
            group = Community(
                name = "Formula 1",
                avatarUrl = Url("https://example.com/f1.png"),
                inviteCode = "f1",
                god = true,
            ),
            memberIds = setOf(MemberId(), MemberId()),
        ),
        GroupWithMembers(
            group = Community(
                name = "Formula 2",
                avatarUrl = Url("https://example.com/f2.png"),
                inviteCode = "f2",
                god = false,
            ),
            memberIds = setOf(MemberId(), MemberId()),
        ),
    )

    private lateinit var groupRepository: GroupRepository

    abstract fun createGroupRepository(): GroupRepository

    @BeforeTest
    fun setup() {
        runTest {
            groupRepository = createGroupRepository()

            for ((initialGroup, memberIds) in initialGroups) {
                groupRepository.add(initialGroup)

                for (memberId in memberIds) {
                    groupRepository.addMember(initialGroup.id, memberId)
                }
            }
        }
    }

    @Test
    fun getShouldReturnGroup() {
        runTest {
            for ((expectedGroup) in initialGroups) {
                val actualGroupResult = groupRepository.get(expectedGroup.id).first()
                actualGroupResult mustBe Success(expectedGroup)
            }
        }
    }

    @Test
    fun getShouldReturnNotFoundIfGroupDoesNotExist() {
        runTest {
            val actualGroupResult = groupRepository.get(GroupId()).first()
            actualGroupResult mustBe Failure(GetGroupEntityError.NotFound)
        }
    }

    @Test
    fun getMembersShouldReturnMembers() {
        runTest {
            for ((initialGroup, memberIds) in initialGroups) {
                val actualMembersResult = groupRepository.getMembers(initialGroup.id).first()
                actualMembersResult mustBe Success(memberIds)
            }
        }
    }

    @Test
    fun getMembersShouldReturnGroupNotFoundIfGroupDoesNotExist() {
        runTest {
            val actualMembersResult = groupRepository.getMembers(GroupId()).first()
            actualMembersResult mustBe Failure(GetGroupMemberEntitiesError.GroupNotFound)
        }
    }

    @Test
    fun addShouldAddGroup() {
        runTest {
            val expectedGroup = Community(
                name = "Formula 3",
                avatarUrl = Url("https://example.com/f3.png"),
                inviteCode = "f3",
                god = false,
            )

            val addResult = groupRepository.add(expectedGroup)
            addResult.mustBeA<UnitSuccess>()

            val actualGroupResult = groupRepository.get(expectedGroup.id).first()
            actualGroupResult mustBe Success(expectedGroup)
        }
    }

    @Test
    fun addShouldReturnDuplicateIdIfGroupIdAlreadyExists() {
        runTest {
            val duplicateGroup = ChatGroup(id = initialGroups[0].group.id)

            val actualAddResult = groupRepository.add(duplicateGroup)
            actualAddResult mustBe Failure(AddGroupEntityError.DuplicateId)
        }
    }

    @Test
    fun addMemberShouldAddMember() {
        runTest {
            val group = initialGroups[0]

            val expectedMemberId = MemberId()
            val addMemberResult = groupRepository.addMember(
                group.group.id,
                expectedMemberId,
            )
            addMemberResult.mustBeA<UnitSuccess>()

            val actualMembersResult = groupRepository.getMembers(group.group.id).first()
            actualMembersResult mustBe Success(group.memberIds + expectedMemberId)
        }
    }

    @Test
    fun addMemberShouldReturnGroupNotFoundIfGroupDoesNotExist() {
        runTest {
            val actualAddMemberResult = groupRepository.addMember(
                GroupId(),
                MemberId(),
            )

            actualAddMemberResult mustBe Failure(AddGroupMemberEntityError.GroupNotFound)
        }
    }
}

private data class GroupWithMembers(
    val group: Group,
    val memberIds: Set<MemberId>,
)